package frame.writer.factory

import frame.writer.FrameWriter
import java.nio.channels.SocketChannel

interface FrameWriterFactory {
    fun create(channel: SocketChannel): FrameWriter
}