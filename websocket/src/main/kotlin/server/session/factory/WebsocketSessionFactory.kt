package websocket.server.session.factory

import http.message.Request
import websocket.server.session.WebsocketSession
import java.nio.channels.SocketChannel

interface WebsocketSessionFactory {
    fun create(channel: SocketChannel, request: Request): WebsocketSession
}