package http.message.factory

import http.message.writer.MessageOutputStreamWriter
import http.message.writer.MessageWriter
import java.net.Socket
import java.nio.channels.SocketChannel

class MessageOutputStreamWriterFactory : MessageWriterFactory {
    override fun create(socket: Socket): MessageWriter {
        return MessageOutputStreamWriter(socket.getOutputStream())
    }

    override fun create(channel: SocketChannel): MessageWriter {
        return MessageOutputStreamWriter(channel.socket().getOutputStream())
    }
}