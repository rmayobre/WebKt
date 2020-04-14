package frame.factory

import ClosureCode
import frame.Frame

/**
 * A factory design pattern for creating a frame that can be sent through any
 * Websocket connection (Session or Websocket).
 * @see DefaultFrameFactory default implementation.
 * @see server.session.Session A WebsocketServer's session for a client Websocket connection.
 * @see client.WebSocket A client-side implementation of a Websocket connection.
 */
interface FrameFactory {
     /** Create a Binary Frame. */
    fun binary(data: ByteArray): Frame

    /** Create a Text Frame. */
    fun text(msg: String): Frame

    /** Create a Ping Frame.  */
    fun ping(data: ByteArray? = null): Frame

    /** Create a Pong Frame. */
    fun pong(data: ByteArray? = null): Frame

    /** Create a Closure Frame. */
    fun close(code: ClosureCode? = null): Frame
}