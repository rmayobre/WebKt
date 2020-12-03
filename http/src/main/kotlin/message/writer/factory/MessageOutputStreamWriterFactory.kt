package message.writer.factory

import message.writer.MessageOutputStreamWriter
import message.writer.MessageWriter
import message.writer.factory.MessageWriterFactory
import java.net.Socket
import java.nio.channels.SocketChannel

@Deprecated("Replaced with Message Channels")
class MessageOutputStreamWriterFactory : MessageWriterFactory {
    override fun create(socket: Socket): MessageWriter {
        return MessageOutputStreamWriter(socket.getOutputStream())
    }

    override fun create(channel: SocketChannel): MessageWriter {
        return MessageOutputStreamWriter(channel.socket().getOutputStream())
    }
}