package frame.writer

import Handshake
import exception.HandshakeException
import exception.WebsocketException
import frame.Frame

interface FrameWriter {
    /**
     * Write a frame to the Websocket's endpoint.
     * @param frame A frame to be sent. If a frame's opcode is closing, then the frame writer must close any internal connections.
     */
    @Throws(WebsocketException::class)
    fun write(frame: Frame)

    /**
     * Perform handshake client endpoint.
     * @param key The key sent from client side.
     * @throws HandshakeException Thrown if handshake could not be complete.
     */
    @Throws(WebsocketException::class)
    fun write(handshake: Handshake)
}