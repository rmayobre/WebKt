package frame.reader.factory

import frame.reader.FrameReader
import java.nio.channels.SocketChannel

interface FrameReaderFactory {
    fun create(channel: SocketChannel): FrameReader
}