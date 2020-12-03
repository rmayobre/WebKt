package message.writer.factory

import message.writer.MessageBufferWriter
import message.writer.MessageWriter
import message.writer.factory.MessageWriterFactory
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