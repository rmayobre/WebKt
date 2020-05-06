package websocket.frame.factory

import websocket.ClosureCode
import websocket.frame.Frame

/**
 * A factory design pattern for creating a websocket.frame that can be sent through any
 * Websocket connection (Session or Websocket).
 * @see DefaultFrameFactory default implementation.
 * @see server.session.Session A WebsocketServer's session for a websocket.client Websocket connection.
 * @see client.Websocket A websocket.client-side implementation of a Websocket connection.
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
    fun close(code: ClosureCode = ClosureCode.NORMAL): Frame
}