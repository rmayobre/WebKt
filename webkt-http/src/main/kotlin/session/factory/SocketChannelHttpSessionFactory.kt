package http.session.factory

import http.Status
import http.message.Response
import http.session.HttpSession
import http.session.SocketChannelHttpSession
import java.nio.channels.SocketChannel

class SocketChannelHttpSessionFactory : HttpSessionFactory<SocketChannel> {
    override fun create(channel: SocketChannel, request: http.message.Request): HttpSession {
        return SocketChannelHttpSession(
            channel = channel,
            request = request,
            response = Response.Builder(Status.INTERNAL_SERVER_ERROR).build())
    }
}