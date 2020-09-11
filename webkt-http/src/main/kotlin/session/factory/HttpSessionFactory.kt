package http.session.factory

import http.message.Request
import http.session.HttpSession
import java.nio.channels.Channel

interface HttpSessionFactory<T: Channel> {
    fun create(channel: T, request: Request): HttpSession
}