package http.session.factory

import http.session.Session
import java.nio.channels.SocketChannel

interface SessionFactory<T> {
    fun create(channel: SocketChannel): Session<T>
}