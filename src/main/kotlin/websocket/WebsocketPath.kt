package websocket

import http.path.Path
import http.exception.HttpException
import http.message.Request
import http.message.Response
import websocket.server.session.Session
import java.lang.Exception
import java.util.concurrent.Executor

abstract class WebsocketPath(override val id: String, private val executor: Executor) : Path {

    private val thread = Thread()


    @Throws(HttpException::class)
    override fun submit(request: Request): Response {
        if (request.isWebsocketUpgrade()) {
            
            TODO("Create websocket session.")
        } else {
            TODO("Return bad request.")
        }
    }

    /** Newly connected Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onConnection(session: Session)

    /** Incoming message from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onMessage(session: Session, message: String)

    /** Incoming data from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onMessage(session: Session, data: ByteArray)

    /** Incoming ping from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onPing(session: Session, data: ByteArray?)

    /** Incoming pong from Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onPong(session: Session, data: ByteArray?)

    /** Session has ended. */
    @Throws(WebsocketException::class)
    protected abstract fun onClose(session: Session, closureCode: ClosureCode)

    /** An error occurred with Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onError(session: Session, ex: WebsocketException)

    /** An unexpected error occurred with provided Session. */
    @Throws(WebsocketException::class)
    protected abstract fun onError(session: Session, ex: Exception)

    /** An unexpected error occurred */
    protected abstract fun onError(ex: Exception)

    private inner class WebsocketRunnable : Runnable {
        override fun run() {
            TODO("Not yet implemented")
        }
    }
    
    companion object {
        private fun Request.websocketKey(): String? = headers["Sec-WebSocket-Key"]
        
        private fun Request.isWebsocketUpgrade(): Boolean {
            return headers["Upgrade"] == "Websocket"
                    && headers["Connection"] == "Upgrade"
                    && headers["Sec-WebSocket-Version"] == "13"
        }
    }
}