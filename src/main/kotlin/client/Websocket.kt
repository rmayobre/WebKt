package client

import ClosureCode
import Handshake
import exception.BadRequestException
import exception.HandshakeException
import exception.WebsocketException
import frame.factory.DefaultFrameFactory
import frame.factory.FrameFactory
import frame.reader.factory.FrameInputStreamReaderFactory
import frame.reader.factory.FrameReaderFactory
import frame.writer.factory.FrameOutputStreamWriterFactory
import frame.writer.factory.FrameWriterFactory
import http.Status
import http.message.Response
import http.message.reader.MessageInputStreamReader
import http.message.writer.MessageOutputStreamWriter
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

open class Websocket(
    protected val handshake: Handshake,
    protected val address: InetSocketAddress,
    protected val frameFactory: FrameFactory,
    protected val readerFactory: FrameReaderFactory,
    protected val writerFactory: FrameWriterFactory,
    protected val listeners: MutableSet<WebsocketListener> = mutableSetOf()
) : WebsocketEventHandler {

    protected lateinit var socket: Socket

    protected lateinit var writer: WebsocketWriter

    protected lateinit var reader: WebsocketReader

    val isClosed: Boolean
        get() = this::socket.isInitialized && socket.isClosed

//    constructor(address: String) : this() TODO create a constructor that takes in a string

//    constructor(address: String, protocols: Array<String>) : this() TODO create a constructor that takes in a string and array of strings.

    constructor(address: InetSocketAddress): this(address,
        Handshake.client("${address.hostName}:${address.port}")
    )

    constructor(address: InetSocketAddress, handshake: Handshake) : this(
        handshake,
        address,
        DefaultFrameFactory(true),
        FrameInputStreamReaderFactory(),
        FrameOutputStreamWriterFactory()
    )

    // TODO test the inital handshake process.
    @Throws(IOException::class)
    fun connect() {
        socket = Socket().apply {
            connect(address) // Throws IOException if cannot connect.
        }

        try {
            val messageWriter = MessageOutputStreamWriter(socket.getOutputStream())
            val messageReader = MessageInputStreamReader(socket.getInputStream())

            messageWriter.write(handshake)
            val response = messageReader.read() // TODO set a timeout

            if (response is Response && response.isWebsocketAccepted()) {
                writer = WebsocketWriter(
                    writerFactory.create(socket),
                    frameFactory,
                    LinkedBlockingQueue(),
                    this)

                reader = WebsocketReader(
                    readerFactory.create(socket),
                    this)
            } else {
                onError(HandshakeException("Server did not send back a proper response -> $response"))
            }
        } catch (ex: Exception) {
            onError(HandshakeException(ex))
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

        private var handshake: Handshake = Handshake.Client("${address.hostName}:${address.port}").build()

        private var frameFactory: FrameFactory = DefaultFrameFactory(true)

        private var readerFactory: FrameReaderFactory = FrameInputStreamReaderFactory()

        private var writerFactory: FrameWriterFactory = FrameOutputStreamWriterFactory()

        private var listeners: MutableSet<WebsocketListener> = mutableSetOf()

        /** Connect to localhost at provided port. */
        constructor(port: Int): this(InetSocketAddress(port))

        /** Connect to host at port. */
        constructor(host: String, port: Int): this(InetSocketAddress(host, port))

        /** Connect to address on provided port. */
        constructor(address: InetAddress, port: Int): this(InetSocketAddress(address, port))

        fun setFrameFactory(factory: FrameFactory) = apply { frameFactory = factory }

        fun setReaderFactory(factory: FrameReaderFactory) = apply { readerFactory = factory }

        fun setWriterFactory(factory: FrameWriterFactory) = apply { writerFactory = factory }

        fun addListener(listener: WebsocketListener) = apply { listeners.add(listener) }

        @Throws(IOException::class)
        fun buildAndConnect(): Websocket = build().also { it.connect() }

        fun build(): Websocket = Websocket(handshake, address, frameFactory, readerFactory, writerFactory, listeners)
    }

    companion object {
        private fun Response.isWebsocketAccepted(): Boolean {
            return status == Status.SWITCH_PROTOCOL &&
                    headers.containsKey("Sec-WebSocket-Accept")
        }
    }
}