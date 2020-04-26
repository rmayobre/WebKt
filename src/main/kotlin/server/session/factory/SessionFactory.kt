package server.session.factory

import exception.BadRequestException
import server.session.Session
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

interface SessionFactory {
    @Throws(BadRequestException::class)
    fun create(channel: SocketChannel, executor: ExecutorService): Session
}