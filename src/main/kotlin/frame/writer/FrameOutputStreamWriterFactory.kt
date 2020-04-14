package frame.writer

import java.nio.channels.SocketChannel

class FrameOutputStreamWriterFactory : FrameWriterFactory {
    override fun create(channel: SocketChannel): FrameWriter {
        return FrameOutputStreamWriter(channel.socket().getOutputStream())
    }
}