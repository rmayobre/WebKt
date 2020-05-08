package websocket.server.session

import websocket.ClosureCode
import websocket.WebsocketException
import websocket.frame.Frame
import http.message.Request
import java.io.IOException
import java.nio.channels.SocketChannel

interface WebsocketSession {
    /** Connection channel to websocket.client. */
    val channel: SocketChannel

    /** The initial request sent from websocket.client. */
    val request: Request

    /** Has the session been closed, or received a closing websocket.frame from websocket.client? */
    val isClosed: Boolean

    /** Read the next Frame from websocket.client. */
    @Throws(WebsocketException::class)
    fun read(): Frame

    /** Send a text message to websocket.client. */
    @Throws(WebsocketException::class)
    fun send(message: String)

    /** Send a ByteArray of data to websocket.client. */
    @Throws(WebsocketException::class)
    fun send(data: ByteArray)

    /** Ping websocket.client connection; optionally send data. */
    @Throws(WebsocketException::class)
    fun ping(data: ByteArray? = null)

    /** Send back a pong to websocket.client; must send data from websocket.client's ping. */
    @Throws(WebsocketException::class)
    fun pong(data: ByteArray? = null)

    /** Close connection with websocket.client, and send session. */
    @Throws(IOException::class)
    fun close(code: ClosureCode)
}