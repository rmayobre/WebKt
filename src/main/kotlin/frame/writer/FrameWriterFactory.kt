package frame.writer

import java.nio.channels.SocketChannel

interface FrameWriterFactory {
    fun create(channel: SocketChannel): FrameWriter
}