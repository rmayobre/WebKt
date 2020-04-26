import client.Websocket
import exception.WebsocketException
import java.net.InetSocketAddress

class WebsocketClient : Websocket(InetSocketAddress(8082)) {

    override fun onOpen() {
        println("New connection open.")
        send("Hello")
    }

    override fun onMessage(message: String) {
        println("Text message received -> $message")
    }

    override fun onMessage(data: ByteArray) {
        println("Binary message received -> $data")
    }

    override fun onPing(data: ByteArray) {
        println("Ping received -> $data")
    }

    override fun onPong(data: ByteArray) {
        println("Pong received -> $data")
    }

    override fun onClose(closureCode: ClosureCode) {
        println("Closing code was sent -> $closureCode")
    }

    override fun onError(exception: WebsocketException) {
        println("An error was sent back -> ${exception.localizedMessage}")
    }
}