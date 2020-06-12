package websocket.server

import http.exception.BadRequestException
import http.exception.HttpException
import http.message.Message
import http.message.Request
import http.route.RunnableRoute
import http.session.HttpSession
import websocket.*
import websocket.frame.OpCode
import websocket.server.session.WebsocketSession
import websocket.server.session.factory.WebsocketSessionChannelFactory
import websocket.server.session.factory.WebsocketSessionFactory
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import kotlin.Exception

abstract class WebsocketRoute(
    override val path: String,
    private val service: ExecutorService,
    private val factory: WebsocketSessionFactory = WebsocketSessionChannelFactory()
) : RunnableRoute {

    private val selector: Selector = Selector.open()

    protected val sessions: MutableSet<WebsocketSession> = mutableSetOf()

    private var running: Boolean = false

    override val isRunning: Boolean
        get() = running

    @Throws(HttpException::class)
    override fun onRoute(session: HttpSession): Message {
        val channel: SocketChannel = session.channel as SocketChannel
        if (session.request.isWebsocketUpgrade()) {
            try {
                val response: Handshake = onHandshake(session.request)
                channel.socket().apply {
                    tcpNoDelay = false
                    keepAlive = true
                }
                val websocketSession: WebsocketSession = factory.create(channel, session.request).also {
                    channel.register(selector, SelectionKey.OP_WRITE, it)
                    sessions.add(it)
                }
                onConnection(websocketSession)
//                session.keepAlive = true
                return response
            } catch (ex: HandshakeException) {
                throw BadRequestException("Handshake failed.")
            }
        } else {
            throw BadRequestException("Request was not a Websocket upgrade.")
        }
    }

    override fun run() {
        running = true
        while (running) {
            try {
                if (selector.selectNow() > 0) {
                    val selectedKeys: Set<SelectionKey> = selector.selectedKeys()
                    selectedKeys.forEach { key ->
                        if (key.isValid) {
                            if (key.isReadable) {
                                service.execute {
                                    val session = key.attachment() as WebsocketSession
                                    try {
                                        val frame = session.read()
                                        when (frame.code) {
                                            OpCode.TEXT -> try {
                                                onMessage(session, frame.data.toString(Charsets.UTF_8))
                                            } catch (ex: UnsupportedEncodingException) {
                                                throw NoUTFException(ex)
                                            }
                                            OpCode.BINARY -> onMessage(session, frame.data)
                                            OpCode.CLOSE -> {
                                                if (frame.length == 0) {
                                                    onClose(session, ClosureCode.NO_STATUS)
                                                } else {
                                                    // TODO Buffer under flow exception thrown here on page refresh.
                                                    if (frame.data.isEmpty()) {
                                                        onClose(session, ClosureCode.NO_STATUS)
                                                    } else {
                                                        val code = ClosureCode.find(ByteBuffer.wrap(frame.data).int)
                                                        onClose(session, code)
                                                    }
                                                }
                                            }
                                            OpCode.PING -> onPing(session, frame.data)
                                            OpCode.PONG -> onPong(session, frame.data)
                                            else -> throw InvalidFrameException(
                                                "Frame's OpCode broke RFC 6455 policy, cannot " +
                                                    "send a continuation frame that is not" +
                                                    " attached to a data frame."
                                            )
                                        }
                                    } catch (ex: WebsocketException) {
                                        onError(session, ex)
                                    } catch (ex: Exception) {
                                        onError(session, ex)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                onError(ex)
            }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        running = false
        selector.close()
        sessions.forEach {
            it.close(ClosureCode.GOING_AWAY)
        }
    }

    protected fun Request.websocketKey(): String? = headers["Sec-WebSocket-Key"]

    /** New websocket upgrade request. */
    @Throws(HandshakeException::class)
    protected abstract fun onHandshake(request: Request): Handshake

    /** Newly connected Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onConnection(session: WebsocketSession)

    /** Incoming message from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onMessage(session: WebsocketSession, message: String)

    /** Incoming data from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onMessage(session: WebsocketSession, data: ByteArray)

    /** Incoming ping from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onPing(session: WebsocketSession, data: ByteArray?)

    /** Incoming pong from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onPong(session: WebsocketSession, data: ByteArray?)

    /** Session has ended. */
    @Throws(WebsocketException::class)
    protected abstract fun onClose(session: WebsocketSession, closureCode: ClosureCode)

    /** An error occurred with Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onError(session: WebsocketSession, ex: WebsocketException)

    /** An unexpected error occurred with provided Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onError(session: WebsocketSession, ex: Exception)

    /** An unexpected error occurred */
    protected abstract fun onError(ex: Exception)

    companion object {
        private fun Request.isWebsocketUpgrade(): Boolean {
            return true
//            return headers["Upgrade"] == "Websocket"
//                && headers["Connection"] == "Upgrade"
//                && headers["Sec-WebSocket-Version"] == "13"
        }
    }
}