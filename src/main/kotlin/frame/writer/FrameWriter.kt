package frame.writer

import exception.HandshakeException
import exception.WebsocketException
import frame.Frame

// TODO Handle client-side masking.
// TODO Should this be closeable?
interface FrameWriter {
    /**
     *
     */
    @Throws(WebsocketException::class)
    fun write(frame: Frame)

    /**
     * Perform handshake client endpoint.
     * @param key The key sent from client side.
     * @throws HandshakeException Thrown if handshake could not be complete.
     */
    @Throws(HandshakeException::class)
    fun writeHandshake(key: String)
}