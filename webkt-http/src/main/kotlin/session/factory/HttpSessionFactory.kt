package http.session.factory

import http.message.Message
import http.message.channel.factory.MessageChannelFactory
import http.session.HttpSession
import http.session.Session
import tcp.ssl.factory.SSLSocketChannelFactory
import java.nio.channels.SocketChannel

class HttpSessionFactory(
    private val channelFactory: MessageChannelFactory,
    private val sslChannelFactory: SSLSocketChannelFactory? = null
) : SessionFactory<Message> {

    override fun create(channel: SocketChannel): Session<Message> = sslChannelFactory?.let {
        TODO("Implement an HTTPS Session class.")
    } ?: HttpSession(channelFactory.create(channel))

}