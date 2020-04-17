package server.session.factory

import ClosureCode
import exception.HandshakeException
import frame.Frame
import frame.factory.DefaultFrameFactory
import frame.reader.FrameReader
import frame.writer.FrameWriter
import frame.factory.FrameFactory
import frame.reader.factory.FrameInputStreamReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.factory.FrameOutputStreamWriterFactory
import frame.writer.factory.FrameWriterFactory
import http.Method
import http.Request
import server.session.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.nio.channels.SocketChannel
import java.util.*

class DefaultSessionFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(),
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

        override fun handshake() {
            request.webSocketKey?.let { key ->
                writer.writeHandshake(key)
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

        @Throws(IllegalArgumentException::class)
        private fun readRequest(channel: SocketChannel): Request {
            val socket = channel.socket()
            val inputStream = socket.getInputStream()
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val requestLine: List<String> = bufferedReader.readLine().split("\\s+")
            val headers: MutableMap<String, String> = hashMapOf()
            var header: String = bufferedReader.readLine()
            while(header.isNotEmpty()) {
                val h: List<String> = header.split(":\\s+", limit = 2)
                headers[h[0]] = h[1]
                header = bufferedReader.readLine()
            }

            return create(requestLine, headers)
        }

        private fun create(requestLine: List<String>, headers: Map<String, String>): Request {
            return Request(
                method = Method.find(requestLine[0]),
                uri = URI(requestLine[1]),
                path = requestLine[1].substring(0, requestLine[1].lastIndexOf("/")+1),
                headers = headers
            )
        }

        /*
        @Throws(IllegalArgumentException::class)
        private fun create(request: ByteArray): Request = create(String(request))

        @Throws(IllegalArgumentException::class)
        private fun create(request: String): Request = create(request.split("\\r?\\n"))

        @Throws(IllegalArgumentException::class)
        private fun create(lines: List<String>): Request {
            val requestLines: List<String> = lines[0].split("\\s+")
            val headers = mutableMapOf<String, String>()
            for (i in 1 until lines.size) {
                val header: List<String> = lines[i].split(Regex(":\\s+"), 2)
                headers[header[0]] = header[1]
            }

            return Request(
                uri = URI(requestLines[1]),
                path = requestLines[1].substring(0, requestLines[1].lastIndexOf("/") + 1),
                method = Method.valueOf(requestLines[0].toUpperCase()),
                headers = mutableMapOf<String, String>().apply {
                    for (i in 1 until lines.size) {
                        val header: List<String> = lines[i].split(Regex(":\\s+"), 2)
                        put(header[0], header[1])
                    }
                })
        }
         */
    }
}