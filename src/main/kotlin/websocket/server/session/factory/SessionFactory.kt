package websocket.server.session.factory

import websocket.BadRequestException
import websocket.server.session.Session
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

interface SessionFactory {
    @Throws(BadRequestException::class)
    fun create(channel: SocketChannel, executor: ExecutorService): Session
}