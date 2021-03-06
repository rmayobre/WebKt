package websocket.frame.reader.factory

import websocket.frame.reader.FrameInputStreamReader
import websocket.frame.reader.FrameReader
import java.net.Socket
import java.nio.channels.SocketChannel

class FrameInputStreamReaderFactory(
    /**
     * The limit for how large a websocket.frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val maxFrameSize: Int = Int.MAX_VALUE
) : FrameReaderFactory {

    override fun create(socket: Socket): FrameReader {
        return FrameInputStreamReader(socket.getInputStream(), maxFrameSize)
    }

    override fun create(channel: SocketChannel): FrameReader {
        return create(channel.socket())
    }
}