package http.message.reader.factory

import http.message.reader.MessageBufferReader
import http.message.reader.MessageReader
import http.message.reader.factory.MessageReaderFactory
import java.net.Socket
import java.nio.channels.SocketChannel

@Deprecated("Replaced with Message Channels")
class MessageBufferReaderFactory(
    private val bufferSize: Int = 256
) : MessageReaderFactory {
    override fun create(socket: Socket): MessageReader {
        return MessageBufferReader(socket.channel, bufferSize)
    }

    override fun create(channel: SocketChannel): MessageReader {
        return MessageBufferReader(channel, bufferSize)
    }
}