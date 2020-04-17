package client

import ClosureCode
import exception.WebsocketException
import frame.OpCode
import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.FrameInputStreamReader
import frame.reader.FrameReader
import frame.writer.FrameOutputStreamWriter
import frame.writer.FrameWriter
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket

open class Websocket(
    private val socket: Socket,
    private val frameFactory: FrameFactory = DefaultFrameFactory(),
//    private val frameReader: FrameReader = FrameInputStreamReader(),
//    private val frameWriter: FrameWriter = FrameOutputStreamWriter(),
    private val listeners: MutableSet<WebsocketListener> = mutableSetOf()
) : WebsocketEventHandler, Closeable {
    //TODO turn into a Class. Create a list of listeners. Make this implement the listener
    // Have the user override the callbacks of the WebSocketListener or create
    // new listeners to handle the callbacks.

//    constructor(address: InetSocketAddress)

    private lateinit var writer: WebsocketWriter

    private lateinit var reader: WebsocketReader

    var isClosed: Boolean = false
        private set

    fun connect() {
        writer = WebsocketWriter(frameFactory)
//        reader = WebsocketReader(frameFactory, this)
        TODO("Send handshake request.")
    }

    fun add(listener: WebsocketListener): Boolean = listeners.add(listener)

    fun remove(listener: WebsocketListener): Boolean = listeners.remove(listener)

    fun clear() = listeners.clear()

    fun send(message: String) {
        TODO("Not yet implemented")
    }

    fun send(data: ByteArray) {
        TODO("Not yet implemented")
    }

    fun ping(data: ByteArray? = null) {
        TODO("Not yet implemented")
    }

    fun pong(data: ByteArray? = null) {
        TODO("Not yet implemented")
    }

    @Synchronized
    fun close(code: ClosureCode) {
        if (!isClosed) {
            TODO("Close socket, then notify listeners of closure.")
        }
    }

    override fun close() {
        close(ClosureCode.NORMAL) // Send close code.
    }

    override fun onOpen() {
        listeners.forEach { it.onOpen(this) }
    }

    override fun onMessage(message: String) {
        listeners.forEach { it.onMessage(this, message) }
    }

    override fun onMessage(data: ByteArray) {
        listeners.forEach { it.onMessage(this, data) }
    }

    override fun onPing(data: ByteArray?) {
        listeners.forEach { it.onPing(this, data) }
    }

    override fun onPong(data: ByteArray?) {
        listeners.forEach { it.onPong(this, data) }
    }

    override fun onClose(closureCode: ClosureCode) {
        listeners.forEach { it.onClose(this, closureCode) }
    }

    override fun onError(exception: WebsocketException) {
        listeners.forEach { it.onError(this, exception) }
    }

}