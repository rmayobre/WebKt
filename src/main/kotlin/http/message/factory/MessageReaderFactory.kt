package http.message.factory

import http.message.reader.MessageReader
import java.net.Socket
import java.nio.channels.SocketChannel

interface MessageReaderFactory {
    fun create(socket: Socket): MessageReader
    fun create(channel: SocketChannel): MessageReader
}