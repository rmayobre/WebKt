package http.message.writer.factory

import http.message.writer.MessageWriter
import java.net.Socket
import java.nio.channels.SocketChannel

@Deprecated("Replaced with Message Channels")
interface MessageWriterFactory {
    fun create(socket: Socket): MessageWriter
    fun create(channel: SocketChannel): MessageWriter
}