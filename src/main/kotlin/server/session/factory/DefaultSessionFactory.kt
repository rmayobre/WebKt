package server.session.factory

import ClosureCode
import exception.HandshakeException
import frame.Frame
import frame.factory.DefaultFrameFactory
import frame.reader.FrameReader
import frame.writer.FrameWriter
import frame.factory.FrameFactory
import frame.reader.FrameInputStreamReaderFactory
import frame.reader.FrameReaderFactory
import frame.writer.FrameOutputStreamWriterFactory
import frame.writer.FrameWriterFactory
import http.Request
import server.session.Session
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*

class DefaultSessionFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(),
    private val writerFactory: FrameWriterFactory = FrameOutputStreamWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameInputStreamReaderFactory()
) : SessionFactory {

    private var buffer: ByteBuffer = ByteBuffer.allocate(REQUEST_BUFFER_SIZE)

    override fun create(channel: SocketChannel): Session {
        return DefaultSession(
            request = readRequest(channel),
            channel = channel,
            factory = frameFactory,
            reader = readerFactory.create(channel),
            writer = writerFactory.create(channel)
        )
    }

    // TODO put readRequest into the init of the Session
    @Throws(IllegalArgumentException::class)
    private fun readRequest(channel: SocketChannel): Request {
        channel.read(buffer)
        val request = Request.create(buffer.array())
        buffer.clear()
        return request
    }

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
            reader.read(false)

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
        private const val REQUEST_BUFFER_SIZE = 1024
    }
}
//internal class WebSocketAcceptor(
//    private val factory: WebSocketSessionFactory,
//    private val serverSocket: ServerSocket
//) : Thread(NAME), Closeable {
//
//    private val sessions: MutableList<Session> = mutableListOf()
//
//    private var listening: Boolean = true
//
//    constructor(factory: WebSocketSessionFactory):
//            this(factory, ServerSocket())
//
//    constructor(factory: WebSocketSessionFactory, port: Int):
//            this(factory, ServerSocket(port))
//
//    // TODO constructor that sets a URI
//
//    override fun run() {
//        while(listening) {
//            try {
//                val client: Socket = serverSocket.accept()
//                val inputStream: InputStream = client.getInputStream()
//                val inputStreamReader = InputStreamReader(inputStream)
//                val bufferedReader = BufferedReader(inputStreamReader)
//
//                val requestLine: List<String> = bufferedReader.readLine().split("\\s+")
//
//                val headers: MutableMap<String, String> = hashMapOf()
//
//                var header: String = bufferedReader.readLine()
//                while(header.isNotEmpty()) {
//                    val h: List<String> = header.split(":\\s+", limit = 2)
//                    headers[h[0]] = h[1]
//                    header = bufferedReader.readLine()
//                }
//
//                val request = Request(
//                    method = Method.find(requestLine[0]),
//                    uri = URI(requestLine[1]),
//                    path = requestLine[1].substring(0, requestLine[1].lastIndexOf("/")+1),
//                    headers = headers
//                )

//                if (request.isWebSocketUpgrade) {
//                    sessions.add(factory.create(request,
//                        reader = FrameInputStreamReader(client.getInputStream()),
//                        writer = FrameOutputStreamWriter(client.getOutputStream())))
//                }
//            } catch (ex: SocketException) {
//                break
//            } catch (ex: IOException) {
//                continue
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    override fun close() {
//        listening = false
//        serverSocket.close()
//        sessions.forEach { it.close() }
//    }
//
//    companion object {
//        const val NAME = "IO-Connection-Thread"
//    }
//}

/*
class SocketChannelAcceptor(
    private val serverSocketChannel: ServerSocketChannel,
    private val queue: BlockingQueue<WebSocketSession>,
    private val acceptSelector: Selector,
    private val processSelector: Selector,
    bufferSize: Int = 256
) : Runnable {

    private var buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

    @Volatile
    private var exit: Boolean = false


    @Throws(IOException::class)
    override fun run() {
        try {
            while (!exit) {
                if (acceptSelector.select() > 0) {
                    val selectedKeys: Set<SelectionKey> = acceptSelector.selectedKeys()
                    selectedKeys.forEach { key ->
                        if (key.isAcceptable) {
                            serverSocketChannel.accept().apply {
                                register(processSelector, SelectionKey.OP_READ)
                                configureBlocking(false)
                            }
                        }
                        if (key.isReadable) {
                            val socketChannel: SocketChannel = key.channel() as SocketChannel
                            val request = readRequest(socketChannel)
                            if (request.isWebSocketUpgrade) {
                                queue.put(factory.create(socketChannel))
                            } else {
                                socketChannel.close()
                            }
                        }
                    }
                }
            }
        } catch (ex: IOException) {
            throw ex // TODO handle exception?
        }
    }

    private fun readRequest(channel: SocketChannel): Request {
        channel.read(buffer)
        val request = Request.create(buffer.array())
        buffer.clear()
        return request
    }

    private fun read(channel: SocketChannel): String {
        channel.read(buffer)
        val message = String(buffer.array()).trim { it <= ' ' }
        buffer.clear()
        return message
    }

    fun stop() {
        exit = true
    }

    companion object {

    }

 */