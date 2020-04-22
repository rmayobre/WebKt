package server.session.factory

import exception.BadRequestException
import server.session.Session
import java.nio.channels.SocketChannel

interface SessionFactory {
    @Throws(BadRequestException::class)
    fun create(channel: SocketChannel): Session
}