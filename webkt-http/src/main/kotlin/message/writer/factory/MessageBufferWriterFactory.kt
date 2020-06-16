package http.message.writer.factory

import http.message.writer.MessageBufferWriter
import http.message.writer.MessageWriter
import http.message.writer.factory.MessageWriterFactory
import java.net.Socket
import java.nio.channels.SocketChannel

@Deprecated("Replaced with Message Channels")
class MessageBufferWriterFactory : MessageWriterFactory {
    override fun create(socket: Socket): MessageWriter {
        return MessageBufferWriter(socket.channel)
    }

    override fun create(channel: SocketChannel): MessageWriter {
        return MessageBufferWriter(channel)
    }
}