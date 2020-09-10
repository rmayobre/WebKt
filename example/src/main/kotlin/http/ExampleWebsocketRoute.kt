package http

import http.message.Request
import websocket.ClosureCode
import websocket.Handshake
import websocket.WebsocketException
import websocket.server.WebsocketRoute
import websocket.server.session.WebsocketSession
import java.lang.Exception
import java.util.concurrent.ExecutorService

class ExampleWebsocketRoute(service: ExecutorService) : WebsocketRoute("/websocket", service) {

    override fun onHandshake(request: Request): Handshake {
        println("$path -> onHandshake: $request")
        return Handshake.Server(request.websocketKey()!!).build()
    }

    override fun onConnection(session: WebsocketSession) {
        println("New WebsocketSession was created.")
    }

    override fun onMessage(session: WebsocketSession, message: String) {
        println("${session.id} (Message): $message")
    }

    override fun onMessage(session: WebsocketSession, data: ByteArray) {
        println("${session.id} (Bytes): $data")
    }

    override fun onPing(session: WebsocketSession, data: ByteArray?) {
        println("${session.id} (Ping): $data")
    }

    override fun onPong(session: WebsocketSession, data: ByteArray?) {
        println("${session.id} (Pong): $data")
    }

    override fun onClose(session: WebsocketSession, closureCode: ClosureCode) {
        println("${session.id} (Connection closed): $closureCode")
    }

    override fun onError(session: WebsocketSession, ex: WebsocketException) {
        println("${session.id} (Websocket Error): ${ex.code} - ${ex.message}")
        println("${session.id} (Stack): ${ex.stackTrace}")
//        session.close(ex.code)
    }

    override fun onError(session: WebsocketSession, ex: Exception) {
        println("${session.id} (Error): ${ex.message}")
        println("${session.id} (Stack): ${ex.stackTrace}")
    }

    override fun onError(ex: Exception) {
        println("Exception: ${ex.message}")
        println("Exception: ${ex.stackTrace}")
    }
}