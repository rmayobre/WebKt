package websocket.frame.writer.factory

import websocket.frame.writer.FrameOutputStreamWriter
import websocket.frame.writer.FrameWriter
import java.net.Socket
import java.nio.channels.SocketChannel

class FrameOutputStreamWriterFactory : FrameWriterFactory {

    override fun create(socket: Socket): FrameWriter {
        return FrameOutputStreamWriter(socket.getOutputStream())
    }

    override fun create(channel: SocketChannel): FrameWriter {
        return create(channel.socket())
    }
}