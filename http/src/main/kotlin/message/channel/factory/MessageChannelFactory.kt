package http.message.channel.factory

import http.message.channel.MessageChannel
import java.nio.channels.SocketChannel

interface MessageChannelFactory {
    fun create(channel: SocketChannel): MessageChannel
}