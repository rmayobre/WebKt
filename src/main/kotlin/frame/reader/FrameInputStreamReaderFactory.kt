package frame.reader

import java.nio.channels.SocketChannel

class FrameInputStreamReaderFactory : FrameReaderFactory {
    override fun create(channel: SocketChannel): FrameReader {
        return FrameInputStreamReader(channel.socket().getInputStream())
    }
}