package server.session.factory

import server.session.Session
import java.nio.channels.SocketChannel

interface SessionFactory {
    fun create(channel: SocketChannel): Session
}