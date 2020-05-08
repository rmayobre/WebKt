package websocket.server

import websocket.ClosureCode
import websocket.WebsocketException
import websocket.server.session.Session
import java.lang.Exception

interface SessionEventHandler {
    /** Newly connected Session. */
    @Throws(WebsocketException::class)
    fun onConnection(session: Session)

    /** Incoming message from Session. */
    @Throws(WebsocketException::class)
    fun onMessage(session: Session, message: String)

    /** Incoming data from Session. */
    @Throws(WebsocketException::class)
    fun onMessage(session: Session, data: ByteArray)

    /** Incoming ping from Session. */
    @Throws(WebsocketException::class)
    fun onPing(session: Session, data: ByteArray?)

    /** Incoming pong from Session. */
    @Throws(WebsocketException::class)
    fun onPong(session: Session, data: ByteArray?)

    /** Session has ended. */
    @Throws(WebsocketException::class)
    fun onClose(session: Session, closureCode: ClosureCode)

    /** An error occurred with Session. */
    @Throws(WebsocketException::class)
    fun onError(session: Session, ex: WebsocketException)

    /** An unexpected error occurred with provided Session. */
    @Throws(WebsocketException::class)
    fun onError(session: Session, ex: Exception)

    /** An unexpected error occurred */
    fun onError(ex: Exception)
}