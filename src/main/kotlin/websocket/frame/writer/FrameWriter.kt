package websocket.frame.writer

import websocket.WebsocketException
import websocket.frame.Frame

interface FrameWriter {
    /**
     * Write a websocket.frame to the Websocket's endpoint.
     * @param frame A websocket.frame to be sent. If a websocket.frame's opcode is closing, then the websocket.frame writer must close any internal connections.
     */
    @Throws(WebsocketException::class)
    fun write(frame: Frame)
}