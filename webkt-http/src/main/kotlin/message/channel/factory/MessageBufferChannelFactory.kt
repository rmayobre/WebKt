package http.message.channel.factory

import http.message.channel.MessageBufferChannel
import http.message.channel.MessageChannel
import java.nio.channels.SocketChannel

class MessageBufferChannelFactory(
        private val bufferSize: Int = DEFAULT_BUFFER_SIZE
): MessageChannelFactory {
    override fun create(channel: SocketChannel): MessageChannel {
        channel.configureBlocking(false)
        return MessageBufferChannel(channel, bufferSize)
    }
}