package server.session.factory

import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.factory.FrameBufferReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.factory.FrameBufferWriterFactory
import frame.writer.factory.FrameWriterFactory
import http.Method
import http.Request
import server.session.DefaultSession
import server.session.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.nio.channels.SocketChannel

class DefaultSessionFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(true),
    private val writerFactory: FrameWriterFactory = FrameBufferWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameBufferReaderFactory()
) : SessionFactory {

    override fun create(channel: SocketChannel): Session = DefaultSession(
        request = readRequest(channel),
        channel = channel,
        factory = frameFactory,
        reader = readerFactory.create(channel),
        writer = writerFactory.create(channel)
    )

    companion object {

        private val HEADER_REGEX: Regex = ":\\s*".toRegex()

        @Throws(IllegalArgumentException::class)
        private fun readRequest(socket: Socket): Request {
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

        private fun readRequest(channel: SocketChannel): Request {
            TODO("Finish request construction.")
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
            TODO("finish creating a request object.")

//            return Request(
//                uri = URI(requestLines[1]),
//                path = requestLines[1].substring(0, requestLines[1].lastIndexOf("/") + 1),
//                method = Method.valueOf(requestLines[0].toUpperCase()),
//                headers = mutableMapOf<String, String>().apply {
//                    for (i in 1 until lines.size) {
//                        val header: List<String> = lines[i].split(Regex(":\\s+"), 2)
//                        put(header[0], header[1])
//                    }
//                })
        }
    }
}