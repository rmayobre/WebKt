package websocket.server.session.factory

import websocket.BadRequestException
import websocket.frame.factory.DefaultFrameFactory
import websocket.frame.factory.FrameFactory
import websocket.frame.reader.factory.FrameBufferReaderFactory
import websocket.frame.reader.factory.FrameReaderFactory
import websocket.frame.writer.factory.FrameBufferWriterFactory
import websocket.frame.writer.factory.FrameWriterFactory
import http.Method
import http.message.Request
import websocket.server.session.SessionChannel
import websocket.server.session.Session
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

class SessionChannelFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(true),
    private val writerFactory: FrameWriterFactory = FrameBufferWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameBufferReaderFactory(),
    private val requestBufferSize: Int = 2048
) : SessionFactory {

    @Throws(BadRequestException::class)
    override fun create(channel: SocketChannel, executor: ExecutorService): Session = SessionChannel(
        request = readRequest(channel),
        channel = channel,
        executor = executor,
        factory = frameFactory,
        reader = readerFactory.create(channel),
        writer = writerFactory.create(channel)
    )

    @Throws(BadRequestException::class)
    private fun readRequest(channel: SocketChannel): Request {
        val headers: MutableMap<String, String> = mutableMapOf()
        val buffer: ByteBuffer = ByteBuffer.allocate(requestBufferSize)

        val requestLine = channel.readRequestLine(buffer)

        var line: String? = channel.readLine(buffer)
        while (line != null && line != "") {
            val (key: String, value: String) = line.split(HEADER_REGEX, 2)
            headers[key] = value
            line = channel.readLine(buffer)
        }

        // TODO update to a request reader.
        val request = try {
            val requestLines = requestLine.split(Regex("\\s"))
            Request(
                method = Method.find(requestLines[0]) ?: throw BadRequestException("Unsupported method type."),
                path = requestLines[1],
                version = requestLines[2],
                line = requestLine,
                headers = headers)
        } catch (ex: Exception) {
            throw BadRequestException("Could not parse an HTTP request's data.", ex)
        }

//        val request = Request(requestLine, headers)
        if (request.method != Method.GET) {
            throw BadRequestException("Websocket requests must bet a GET method.")
        }
        return request
    }

    companion object {

        private val HEADER_REGEX = Regex(":\\s*")

        @Throws(BadRequestException::class)
        private fun SocketChannel.readRequestLine(buffer: ByteBuffer): String {
            if (read(buffer) == -1) {
                throw BadRequestException("Could not read request.")
            } else {
                buffer.flip()
                return readLine(buffer) ?: throw BadRequestException("An empty request was sent.")
            }
        }

        /**
         * Read the next line of the request.
         * @return the next line in the buffer; will return null end of line or line did not end with a new line.
         */
        private fun SocketChannel.readLine(buffer: ByteBuffer): String? {
            val builder = StringBuilder()

            var prev = ' '
            do {
                while (buffer.hasRemaining()) {
                    val current = buffer.get().toChar()
                    if (prev == '\r' && current == '\n') {
                        return builder.substring(0, builder.length - 1)
                    }
                    builder.append(current)
                    prev = current
                }
                buffer.clear()
            } while (read(buffer).also { buffer.flip() } != -1)

            return null
        }

    }
}