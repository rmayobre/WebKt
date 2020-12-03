package session.factory

import session.Session
import TypeChannel
import java.nio.channels.SocketChannel

interface SessionFactory<T> {
    fun create(channel: SocketChannel): Session<T>
}