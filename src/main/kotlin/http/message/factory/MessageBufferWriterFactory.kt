package http.message.factory

import http.message.writer.MessageWriter
import java.net.Socket
import java.nio.channels.SocketChannel

class MessageBufferWriterFactory : MessageWriterFactory {
    override fun create(socket: Socket): MessageWriter {
        TODO("Not yet implemented")
    }

    override fun create(channel: SocketChannel): MessageWriter {
        TODO("Not yet implemented")
    }
}