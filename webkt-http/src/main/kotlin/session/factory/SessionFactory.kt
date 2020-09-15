package http.session.factory

import http.session.Session
import java.nio.channels.SocketChannel

interface SessionFactory {
    fun create(channel: SocketChannel): Session
}