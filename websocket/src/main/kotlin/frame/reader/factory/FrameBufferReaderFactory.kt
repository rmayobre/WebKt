package websocket.frame.reader.factory

import websocket.InternalErrorException
import websocket.frame.reader.FrameBufferReader
import websocket.frame.reader.FrameReader
import java.net.Socket
import java.nio.channels.SocketChannel

class FrameBufferReaderFactory(
    /**
     * The limit for how large a websocket.frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val maxFrameSize: Int = Int.MAX_VALUE,
    private val bufferSize: Int = 256
) : FrameReaderFactory {
    override fun create(socket: Socket): FrameReader {
        socket.channel?.let { channel ->
            return create(channel)
        } ?: throw InternalErrorException("There was no SocketChannel linked to Socket.")
    }

    override fun create(channel: SocketChannel): FrameReader {
        return FrameBufferReader(channel, maxFrameSize, bufferSize)
    }
}