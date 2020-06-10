package websocket.frame.writer.factory

import websocket.InternalErrorException
import websocket.frame.writer.FrameBufferWriter
import websocket.frame.writer.FrameWriter
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