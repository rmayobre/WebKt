package frame.reader.factory

import frame.reader.FrameInputStreamReader
import frame.reader.FrameReader
import java.nio.channels.SocketChannel

class FrameInputStreamReaderFactory(
    /**
     * The limit for how large a frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val frameSizeLimit: Int = Int.MAX_VALUE
) : FrameReaderFactory {
    override fun create(channel: SocketChannel): FrameReader {
        return FrameInputStreamReader(channel.socket().getInputStream(), frameSizeLimit)
    }
}