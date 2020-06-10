package websocket.frame.reader

import websocket.WebsocketException
import websocket.frame.Frame
import java.io.Closeable

interface FrameReader : Closeable {
    /**
     * Reads the WebSocketIO's input stream of data and produces a list of fragments.
     * These fragments will add up to become one websocket.frame of data.
     *
     * @param requiresMask If the stream is being used for a WebSocketServer, masking is required.
     *                    For websocket.client-side WebSocket connections, frames sent to a websocket.client are not
     *                    required to be masked. Frames sent from a websocket.client are required to be masked!
     * @return List of websocket.frame fragments. The data needs to be combined to be usable.
     * @throws WebsocketException when frames where not masked or input stream was corrupted.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.1">RFC 6455, Section 5.1 Overview</a>
     */
    @Throws(WebsocketException::class)
    fun read(requiresMask: Boolean): Frame
}