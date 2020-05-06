package http.message.factory

import http.message.reader.MessageInputStreamReader
import http.message.reader.MessageReader
import java.net.Socket
import java.nio.channels.SocketChannel

class MessageInputStreamReaderFactory : MessageReaderFactory {
    override fun create(socket: Socket): MessageReader {
        return MessageInputStreamReader(socket.getInputStream())
    }

    override fun create(channel: SocketChannel): MessageReader {
        return MessageInputStreamReader(channel.socket().getInputStream())
    }
}