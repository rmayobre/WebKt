import example.WebsocketServer
import websocket.ClosureCode
import websocket.WebsocketException
import websocket.client.Websocket
import websocket.client.WebsocketListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress

class WebsocketServerLiveTester

private val server = WebsocketServer()

private val client = Websocket(InetSocketAddress(8082)).apply {
    add(object : WebsocketListener {
        override fun onOpen(websocket: Websocket) {
            println("New connection open.")
            websocket.send("Hello")
        }

        override fun onMessage(websocket: Websocket, message: String) {
            println("Text message received -> $message")
        }

        override fun onMessage(websocket: Websocket, data: ByteArray) {
            println("Binary message received -> $data")
        }

        override fun onPing(websocket: Websocket, data: ByteArray) {
            println("Ping received -> $data")
        }

        override fun onPong(websocket: Websocket, data: ByteArray) {
            println("Pong received -> $data")
        }

        override fun onClose(websocket: Websocket, closureCode: ClosureCode) {
            println("Closing code was sent -> $closureCode")
        }

        override fun onError(websocket: Websocket, exception: WebsocketException) {
            println("An error was sent back -> ${exception.localizedMessage}")
        }
    })
}

fun main() {
    server.start()
    println("Type \"exit\" to stop program...")
    client.connect()
    val isr = InputStreamReader(System.`in`)
    val bufferedReader = BufferedReader(isr)
    var line = bufferedReader.readLine()
    while (line != "exit") {
        line = bufferedReader.readLine()
    }
    server.stop()
}
