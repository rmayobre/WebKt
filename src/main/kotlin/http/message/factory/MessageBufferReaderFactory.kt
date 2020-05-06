package http.message.factory

import http.message.reader.MessageReader
import java.net.Socket
import java.nio.channels.SocketChannel

class MessageBufferReaderFactory : MessageReaderFactory {
    override fun create(socket: Socket): MessageReader {
        TODO("Not yet implemented")
    }

    override fun create(channel: SocketChannel): MessageReader {
        TODO("Not yet implemented")
    }
}