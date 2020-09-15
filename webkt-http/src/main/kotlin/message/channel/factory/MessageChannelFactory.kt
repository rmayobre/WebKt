package http.message.channel.factory

import http.message.channel.MessageChannel
import java.nio.channels.ByteChannel
import java.nio.channels.SocketChannel

interface MessageChannelFactory {
    fun create(channel: ByteChannel): MessageChannel
}