package client

import ClosureCode
import frame.reader.FrameInputStreamReader
import frame.writer.FrameOutputStreamWriter
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import java.net.URI
import java.net.UnknownHostException

class WebSocketImpl(endpoint: URI) : WebSocket {

    private val listeners = mutableSetOf<WebSocketListener>()

    private val socket = Socket(endpoint.host, endpoint.port)

    private val output = FrameOutputStreamWriter(socket.getOutputStream())

    private val input = FrameInputStreamReader(socket.getInputStream())

    private lateinit var thread: Thread

    fun add(listener: WebSocketListener): Boolean = listeners.add(listener)

    fun remove(listener: WebSocketListener): Boolean =  listeners.remove(listener)

    fun notifyListeners(callback: (listener: WebSocketListener) -> Unit) = synchronized(listeners) {
        listeners.forEach { callback(it) }
    }

    override fun send(message: String) {

    }

    override fun send(data: ByteArray) {
        output.write(FrameTest())
    }

    override fun ping(data: ByteArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close(code: ClosureCode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun run() {
        try {

        } catch (ex: Exception) {

        }
    }

    @Synchronized
    override fun close() {
        input.close()
        output.close()
        socket.close()
    }

    companion object {
        @Throws(UnknownHostException::class, IOException::class)
        fun connect(endpoint: String): WebSocketImpl = WebSocketImpl(URI(endpoint))

        @Throws(UnknownHostException::class, IOException::class)
        fun connect(host: String, port: String): WebSocketImpl = WebSocketImpl(URI("$host:$port"))
    }
}