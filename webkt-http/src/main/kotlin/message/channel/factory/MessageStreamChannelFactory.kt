package http.message.channel.factory

import http.message.channel.MessageChannel
import http.message.channel.MessageStreamChannel
import java.nio.channels.SocketChannel

class MessageStreamChannelFactory : MessageChannelFactory {
    override fun create(channel: SocketChannel): MessageChannel {
        channel.configureBlocking(true)
        return MessageStreamChannel(channel)
    }
}