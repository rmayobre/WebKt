package websocket.frame.reader.factory

import websocket.frame.reader.FrameReader
import java.net.Socket
import java.nio.channels.SocketChannel

interface FrameReaderFactory {
    fun create(socket: Socket): FrameReader
    fun create(channel: SocketChannel): FrameReader
}