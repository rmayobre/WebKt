import exception.WebsocketException
import server.SessionChanneler
import server.SessionEventHandler
import server.session.factory.DefaultSessionFactory
import server.session.Session
import server.session.factory.SessionFactory
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

    private val handlers = mutableSetOf<SessionEventHandler>()

    constructor(address: InetSocketAddress) : this(
        address = address,
        factory = DefaultSessionFactory(),
        executor = Executors.newSingleThreadExecutor())

    /** Construct locally to port 80 */
    constructor(): this(InetSocketAddress(8080))

    fun start() {
        println("Starting server on address -> $address")
        sessionMap = mutableMapOf()
        channeler = SessionChanneler(factory, executor, address, this)
        channeler.start()
    }

    fun stop() { // TODO notify sessions of closure?
        println("Now shutting down server...")
        channeler.close()
    }

    fun register(handler: SessionEventHandler): Boolean = handlers.add(handler)

    fun remove(handler: SessionEventHandler): Boolean = handlers.remove(handler)

    fun clearHandlers() {
        handlers.clear()
    }

    override fun onConnection(session: Session): Boolean {
        println("New session has connected...")
        println("Request sent -> ${session.request}")
//        for (handler in handlers) {
//            if (!handler.onConnection(session)) {
//                return false
//            }
//        }
//        sessionMap[session.key] = session
        return true
    }

    override fun onMessage(session: Session, message: String) {
        println("Text message was received -> $message")
//        for (handler in handlers) {
//            handler.onMessage(session, message)
//        }
    }

    override fun onMessage(session: Session, data: ByteArray) {
        println("Binary message  was received -> ${data.toString(Charsets.UTF_8)}")
//        for (handler in handlers) {
//            handler.onMessage(session, data)
//        }
    }

    override fun onPing(session: Session, data: ByteArray?) {
        println("Ping was received -> ${data?.toString(Charsets.UTF_8)}")
//        for (handler in handlers) {
//            handler.onPing(session, data)
//        }
    }

    override fun onPong(session: Session, data: ByteArray?) {
        println("Pong was received -> ${data?.toString(Charsets.UTF_8)}")
//        for (handler in handlers) {
//            handler.onPong(session, data)
//        }
    }

    override fun onClose(session: Session, closureCode: ClosureCode) {
        println("Closing code was received -> $closureCode")
//        for (handler in handlers) {
//            handler.onClose(session, closureCode)
//        }
    }

    override fun onError(session: Session, ex: WebsocketException) {
        println("An error occurred -> ${ex.localizedMessage}")

//        for (handler in handlers) {
//            handler.onError(session, ex)
//        }
        session.close(ex.code)
        onClose(session, ex.code)
    }
}