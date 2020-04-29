package client

import ClosureCode
import Handshake
import exception.WebsocketException
import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.factory.FrameInputStreamReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.factory.FrameOutputStreamWriterFactory
import frame.writer.factory.FrameWriterFactory
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

open class Websocket(
    protected val address: InetSocketAddress,
    protected val frameFactory: FrameFactory,
    protected val readerFactory: FrameReaderFactory,
    protected val writerFactory: FrameWriterFactory,
    protected val listeners: MutableSet<WebsocketListener>
) : WebsocketEventHandler {

    private lateinit var socket: Socket

    private lateinit var writer: WebsocketWriter

    private lateinit var reader: WebsocketReader

    var isClosed: Boolean = false
        private set

    constructor(address: InetSocketAddress): this(
        address,
        DefaultFrameFactory(true),
        FrameInputStreamReaderFactory(),
        FrameOutputStreamWriterFactory(),
        mutableSetOf())

    @Throws(IOException::class)
    fun connect(path: String = "/") = connect(Handshake.default(
        host = "${address.hostName}:${address.port}",
        path = path,
        key = "")) // TODO build key.

    @Throws(IOException::class)
    fun connect(handshake: Handshake) {
        socket = Socket().apply {
            connect(address)
        }.also {
            isClosed = false
            writer = WebsocketWriter(
                writerFactory.create(it),
                frameFactory,
                LinkedBlockingQueue(),
                this)

            try {
                writer.handshake(handshake)
                reader = WebsocketReader(readerFactory.create(it), this)
            } catch (ex: WebsocketException) {
                close(ex.code)
                onError(ex)
            }
        }
    }

    fun send(message: String) {
        writer.send(message)
    }

    fun send(data: ByteArray) {
        writer.send(data)
    }

    fun ping(data: ByteArray? = null) {
        writer.ping(data)
    }

    fun pong(data: ByteArray? = null) {
        writer.pong(data)
    }

    @Synchronized
    fun close(code: ClosureCode = ClosureCode.NORMAL) {
        if (!isClosed) {
            reader.close()
            writer.close(code)
        }
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

    override fun onPing(data: ByteArray) {
        listeners.forEach { it.onPing(this, data) }
    }

    override fun onPong(data: ByteArray) {
        listeners.forEach { it.onPong(this, data) }
    }

    override fun onClose(closureCode: ClosureCode) {
        socket.close()
        listeners.forEach { it.onClose(this, closureCode) }
    }

    override fun onError(exception: WebsocketException) {
        listeners.forEach { it.onError(this, exception) }
    }

    data class Builder(private val address: InetSocketAddress) {

        private var frameFactory: FrameFactory = DefaultFrameFactory(true)

        private var readerFactory: FrameReaderFactory = FrameInputStreamReaderFactory()

        private var writerFactory: FrameWriterFactory = FrameOutputStreamWriterFactory()

        private var listeners: MutableSet<WebsocketListener> = mutableSetOf()

        constructor(port: Int): this(InetSocketAddress(port))

        constructor(host: String, port: Int): this(InetSocketAddress(host, port))

        constructor(address: InetAddress, port: Int): this(InetSocketAddress(address, port))

        fun setFrameFactory(factory: FrameFactory) = apply { frameFactory = factory }

        fun setReaderFactory(factory: FrameReaderFactory) = apply { readerFactory = factory }

        fun setWriterFactory(factory: FrameWriterFactory) = apply { writerFactory = factory }

        fun addListener(listener: WebsocketListener) = apply { listeners.add(listener) }

        @Throws(IOException::class)
        fun build(handshake: Handshake): Websocket = build().also { it.connect(handshake) }

        fun build(): Websocket = Websocket(address, frameFactory, readerFactory, writerFactory, listeners)
    }

    companion object {
        private inline fun Websocket.safeCall(block: () -> Unit) {
            try {
                return block()
            } catch (ex: WebsocketException) {
                close(ex.code)
                onError(ex)
            }
        }
    }
}