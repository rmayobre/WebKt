package http.message.factory

import http.message.writer.MessageWriter
import java.net.Socket
import java.nio.channels.SocketChannel

interface MessageWriterFactory {
    fun create(socket: Socket): MessageWriter
    fun create(channel: SocketChannel): MessageWriter
}