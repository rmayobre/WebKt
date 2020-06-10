package websocket.frame.writer.factory

import websocket.frame.writer.FrameWriter
import java.net.Socket
import java.nio.channels.SocketChannel

interface FrameWriterFactory {
    fun create(socket: Socket): FrameWriter
    fun create(channel: SocketChannel): FrameWriter
}