package server.session

import ClosureCode
import exception.HandshakeException
import exception.WebsocketException
import frame.Frame
import http.message.Request
import java.io.IOException
import java.nio.channels.SocketChannel

interface Session {
    /** Connection channel to client. */
    val channel: SocketChannel

    /** The initial request sent from client. */
    val request: Request

    /** Has the session been closed, or received a closing frame from client? */
    val isClosed: Boolean

    /** Read the next Frame from client. */
    @Throws(WebsocketException::class)
    fun read(): Frame

    /** Shake hands with client connection. */
    @Throws(HandshakeException::class)
    fun handshake(headers: Map<String, String>? = null)

    /** Send a text message to client. */
    @Throws(WebsocketException::class)
    fun send(message: String)

    /** Send a ByteArray of data to client. */
    @Throws(WebsocketException::class)
    fun send(data: ByteArray)

    /** Ping client connection; optionally send data. */
    @Throws(WebsocketException::class)
    fun ping(data: ByteArray? = null)

    /** Send back a pong to client; must send data from client's ping. */
    @Throws(WebsocketException::class)
    fun pong(data: ByteArray? = null)

    /** Close connection with client, and send session. */
    @Throws(IOException::class)
    fun close(code: ClosureCode)
}