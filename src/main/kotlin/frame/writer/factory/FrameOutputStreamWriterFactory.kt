package frame.writer.factory

import frame.writer.FrameOutputStreamWriter
import frame.writer.FrameWriter
import java.nio.channels.SocketChannel

class FrameOutputStreamWriterFactory : FrameWriterFactory {
    override fun create(channel: SocketChannel): FrameWriter {
        return FrameOutputStreamWriter(channel.socket().getOutputStream())
    }
}