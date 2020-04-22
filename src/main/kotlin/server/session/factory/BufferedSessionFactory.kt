package server.session.factory

import exception.BadRequestException
import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.factory.FrameBufferReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.factory.FrameBufferWriterFactory
import frame.writer.factory.FrameWriterFactory
import http.Method
import http.Request
import server.session.BufferedSession
import server.session.Session
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class BufferedSessionFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(true),
    private val writerFactory: FrameWriterFactory = FrameBufferWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameBufferReaderFactory(),
    private val requestBufferSize: Int = 2048
) : SessionFactory {

    @Throws(BadRequestException::class)
    override fun create(channel: SocketChannel): Session = BufferedSession(
        request = readRequest(channel),
        channel = channel,
        factory = frameFactory,
        reader = readerFactory.create(channel),
        writer = writerFactory.create(channel)
    )

    @Throws(BadRequestException::class)
    private fun readRequest(channel: SocketChannel): Request {
        val headers: MutableMap<String, String> = mutableMapOf()
        val buffer: ByteBuffer = ByteBuffer.allocate(requestBufferSize)

        val requestLines: List<String> = channel.readRequestLines(buffer)

        var line: String? = channel.readLine(buffer)
        while (line != null && line != "") {
            val (key: String, value: String) = line.split(HEADER_REGEX, 2)
            headers[key] = value
            line = channel.readLine(buffer)
        }

        return Request(
            method = Method.find(requestLines[0]).also { if (it != Method.GET) throw BadRequestException("Websocket requests must bet a GET method.") },
            path = requestLines[1],
            version = requestLines[2],
            headers = headers)
    }

    @Throws(BadRequestException::class)
    private fun SocketChannel.readRequestLines(buffer: ByteBuffer): List<String> {
        if (read(buffer) == -1) {
            throw BadRequestException("Could not read request.")
        } else {
            buffer.flip()
            return readLine(buffer)?.splitByWhitespace()
                ?: throw BadRequestException("An empty request was sent.")
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

    companion object {

        private val HEADER_REGEX = Regex(":\\s*")

        private val WHITESPACE_REGEX = Regex("\\s")

        /** Split a String into a list of Strings separated by whitespace. */
        private fun String.splitByWhitespace() = split(WHITESPACE_REGEX)

//        @Throws(IllegalArgumentException::class)
//        private fun readRequest(socket: Socket): Request {
//            val inputStream = socket.getInputStream()
//            val inputStreamReader = InputStreamReader(inputStream)
//            val bufferedReader = BufferedReader(inputStreamReader)
//
//            val requestLine: List<String> = bufferedReader.readLine().split("\\s+".toRegex())
//            val headers: MutableMap<String, String> = hashMapOf()
//            var header: String = bufferedReader.readLine()
//            while(header.isNotEmpty()) {
//                println(header)
//                val h: List<String> = header.split(HEADER_REGEX, 2)
//                headers[h[0]] = h[1]
//                header = bufferedReader.readLine()
//            }
//
//            return create(requestLine, headers)
//        }
//        private fun create(requestLine: List<String>, headers: Map<String, String>): Request {
//    // TODO check http version in request. Throw exception if request is not 1.1 or greater.
//    // TODO check if method is GET. Method must be GET, otherwise throw exception.
//    return Request(
//        method = Method.find(requestLine[0]),//.also { if (it != Method.GET) throw WebsocketException },
//        path = requestLine[1],
//        version = requestLine[2],
//        headers = headers
//    )
//}
    }
}