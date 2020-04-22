package frame.writer.factory

import exception.InternalErrorException
import frame.writer.FrameBufferWriter
import frame.writer.FrameWriter
import java.net.Socket
import java.nio.channels.SocketChannel

class FrameBufferWriterFactory : FrameWriterFactory {
    override fun create(socket: Socket): FrameWriter {
        socket.channel?.let { channel ->
            return create(channel)
        } ?: throw InternalErrorException("There was no SocketChannel linked to Socket.")
    }

    override fun create(channel: SocketChannel): FrameWriter {
        return FrameBufferWriter(channel)
    }
}