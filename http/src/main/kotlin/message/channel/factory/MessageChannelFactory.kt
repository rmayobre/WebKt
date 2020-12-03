package message.channel.factory

import message.channel.MessageChannel
import java.nio.channels.ByteChannel

interface MessageChannelFactory {
    fun create(channel: ByteChannel): MessageChannel
}