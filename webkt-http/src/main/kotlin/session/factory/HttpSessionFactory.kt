package http.session.factory

import http.session.HttpSession
import java.nio.channels.Channel

interface HttpSessionFactory<T: Channel> {
    fun create(channel: T, request: http.message.Request): HttpSession
}