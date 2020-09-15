package http.message.channel.factory

import http.message.channel.MessageChannel
import java.nio.channels.ByteChannel

interface MessageChannelFactory {
    fun create(channel: ByteChannel): MessageChannel
}