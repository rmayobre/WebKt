package example

import http.message.Request
import websocket.ClosureCode
import websocket.Handshake
import websocket.WebsocketException
import websocket.server.WebsocketPath
import websocket.server.session.WebsocketSession
import websocket.server.session.factory.WebsocketSessionChannelFactory
import websocket.server.session.factory.WebsocketSessionFactory
import java.lang.Exception
import java.util.concurrent.ExecutorService

class ExampleWebsocketPath private constructor(
    path: String,
    service: ExecutorService,
    factory: WebsocketSessionFactory
) : WebsocketPath(path, service, factory) {

    constructor(path: String, service: ExecutorService):
            this(path, service, WebsocketSessionChannelFactory())

    override fun onHandshake(request: Request): Handshake {
        TODO("Not yet implemented")
    }

    override fun onConnection(session: WebsocketSession) {
        TODO("Not yet implemented")
    }

    override fun onMessage(session: WebsocketSession, message: String) {
        TODO("Not yet implemented")
    }

    override fun onMessage(session: WebsocketSession, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun onPing(session: WebsocketSession, data: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onPong(session: WebsocketSession, data: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onClose(session: WebsocketSession, closureCode: ClosureCode) {
        TODO("Not yet implemented")
    }

    override fun onError(session: WebsocketSession, ex: WebsocketException) {
        TODO("Not yet implemented")
    }

    override fun onError(session: WebsocketSession, ex: Exception) {
        TODO("Not yet implemented")
    }

    override fun onError(ex: Exception) {
        TODO("Not yet implemented")
    }
}