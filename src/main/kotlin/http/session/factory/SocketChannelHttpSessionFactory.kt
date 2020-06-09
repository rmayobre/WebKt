package http.session.factory

import http.session.HttpSession
import http.session.SocketChannelHttpSession
import java.nio.channels.SocketChannel

class SocketChannelHttpSessionFactory : HttpSessionFactory<SocketChannel> {
    override fun create(channel: SocketChannel, request: http.message.Request): HttpSession {
        return SocketChannelHttpSession(channel, request)
    }
}