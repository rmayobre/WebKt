package frame.reader

import java.nio.channels.SocketChannel

interface FrameReaderFactory {
    fun create(channel: SocketChannel): FrameReader
}