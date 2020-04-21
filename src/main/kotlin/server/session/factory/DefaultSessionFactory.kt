package server.session.factory

import ClosureCode
import exception.HandshakeException
import frame.Frame
import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.FrameReader
import frame.reader.factory.FrameInputStreamReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.FrameWriter
import frame.writer.factory.FrameOutputStreamWriterFactory
import frame.writer.factory.FrameWriterFactory
import http.Method
import http.Request
import server.session.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.channels.SocketChannel
import java.util.*

class DefaultSessionFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(true),
    private val writerFactory: FrameWriterFactory = FrameOutputStreamWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameInputStreamReaderFactory()
) : SessionFactory {

    override fun create(channel: SocketChannel): Session = DefaultSession(
        request = readRequest(channel),
        channel = channel,
        factory = frameFactory,
        reader = readerFactory.create(channel),
        writer = writerFactory.create(channel)
    )

    private inner class DefaultSession(
        override val request: Request,
        override val channel: SocketChannel,
        private val factory: FrameFactory,
        private val reader: FrameReader,
        private val writer: FrameWriter
    ) : Session {

        private val frameQueue: Queue<Frame> = LinkedList()

        private var _isClosed: Boolean = false

        override val isClosed: Boolean
            get() = _isClosed

        override val isWriteable: Boolean
            get() = frameQueue.isNotEmpty()

        override fun read(): Frame =
            reader.read(true)

        override fun write() {
            frameQueue.poll()?.let {
                writer.write(it)
            }
        }

        override fun handshake(headers: Map<String, String>?) {
            request.webSocketKey?.let { key ->
                val handshake = Handshake.Server(key).apply {
                    headers?.let {
                        it.forEach { (key, value) ->
                            addHeader(key, value)
                        }
                    }
                }.build()
                writer.write(handshake)
            } ?: throw HandshakeException("Request did not provided a key.")
        }

        override fun send(message: String) {
            frameQueue.add(factory.text(message))
        }

        override fun send(data: ByteArray) {
            frameQueue.add(factory.binary(data))
        }

        override fun ping(data: ByteArray?) {
            frameQueue.add(factory.ping(data))
        }

        override fun pong(data: ByteArray?) {
            frameQueue.add(factory.pong(data))
        }

        override fun close(code: ClosureCode) {
            _isClosed = true
            reader.close()
            frameQueue.clear()
            frameQueue.add(factory.close(code))
        }

    }

    companion object {

        private val HEADER_REGEX: Regex = ":\\s*".toRegex()

        @Throws(IllegalArgumentException::class)
        private fun readRequest(channel: SocketChannel): Request {
            val socket = channel.socket()
            val inputStream = socket.getInputStream()
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val requestLine: List<String> = bufferedReader.readLine().split("\\s+".toRegex())
            val headers: MutableMap<String, String> = hashMapOf()
            var header: String = bufferedReader.readLine()
            while(header.isNotEmpty()) {
                println(header)
                val h: List<String> = header.split(HEADER_REGEX, 2)
                headers[h[0]] = h[1]
                header = bufferedReader.readLine()
            }

            return create(requestLine, headers)
        }

        private fun create(requestLine: List<String>, headers: Map<String, String>): Request {
            // TODO check http version in request. Throw exception if request is not 1.1 or greater.
            // TODO check if method is GET. Method must be GET, otherwise throw exception.
            return Request(
                method = Method.find(requestLine[0]),//.also { if (it != Method.GET) throw WebsocketException },
                path = requestLine[1],
                version = requestLine[2],
                headers = headers
            )
        }
    }
}