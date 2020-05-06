package example

import websocket.WebsocketException
import websocket.server.SessionChanneler
import websocket.server.SessionEventHandler
import websocket.server.session.factory.SessionChannelFactory
import websocket.server.session.Session
import websocket.server.session.factory.SessionFactory
import websocket.ClosureCode
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class WebsocketServer(
    private val address: InetSocketAddress,
    private val factory: SessionFactory,
    private val executor: ExecutorService
) : SessionEventHandler {

    protected lateinit var sessionMap: MutableMap<String, Session>

    private lateinit var channeler: SessionChanneler

    constructor(address: InetSocketAddress) : this(
        address = address,
        factory = SessionChannelFactory(),
        executor = Executors.newSingleThreadExecutor())

    /** Construct locally to port 80 */
    constructor(): this(InetSocketAddress(8082))

    fun start() {
        println("Starting websocket.server on address -> $address")
        sessionMap = mutableMapOf()
        channeler = SessionChanneler(factory, executor, address, this)
        channeler.start()
    }

    fun stop() {
        println("Now shutting down websocket.server...")
        channeler.close()
    }

    override fun onConnection(session: Session) {
        println("New session has connected...")
        println("Request sent -> ${session.request}")
        session.handshake()
    }

    override fun onMessage(session: Session, message: String) {
        println("Text message was received -> $message")
        session.send(message)
    }

    override fun onMessage(session: Session, data: ByteArray) {
        println("Binary message  was received -> ${data.toString(Charsets.UTF_8)}")
        session.send(data)
    }

    override fun onPing(session: Session, data: ByteArray?) {
        println("Ping was received -> ${data?.toString(Charsets.UTF_8)}")
    }

    override fun onPong(session: Session, data: ByteArray?) {
        println("Pong was received -> ${data?.toString(Charsets.UTF_8)}")
    }

    override fun onClose(session: Session, closureCode: ClosureCode) {
        println("Closing code was received -> $closureCode")
    }

    override fun onError(session: Session, ex: WebsocketException) {
        throw ex
//        println("An error occurred -> ${ex.message}")
    }

    override fun onError(session: Session, ex: Exception) {
        throw ex
//        println("An error occurred -> ${ex.message}")
    }

    override fun onError(ex: Exception) {
        throw ex
//        println("An error occurred -> ${ex.message}")
    }
}