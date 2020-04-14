package server

import ClosureCode
import exception.WebsocketException
import server.session.Session

interface SessionEventHandler {
    /** Newly connected Session. */
    @Throws(WebsocketException::class)
    fun onConnection(session: Session): Boolean

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
}