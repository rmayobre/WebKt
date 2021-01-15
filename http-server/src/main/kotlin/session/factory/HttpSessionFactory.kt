package session.factory

import message.Message
import message.channel.factory.MessageChannelFactory
import session.HttpSession
import session.Session
import old.ssl.factory.SSLSocketChannelFactory
import java.nio.channels.SocketChannel
import java.util.*

class DefaultHttpSessionFactory(
    private val channelFactory: MessageChannelFactory,
    private val sslChannelFactory: SSLSocketChannelFactory? = null
) : SessionFactory<Message> {

    override fun create(channel: SocketChannel): Session<Message> {
        val sslSocketChannel = sslChannelFactory?.create(channel)
        return HttpSession(
            id = UUID.randomUUID().toString(),
            channel = channelFactory.create(
                sslSocketChannel ?: channel
            ),
            sslSession = sslSocketChannel?.session
        )
    }
}