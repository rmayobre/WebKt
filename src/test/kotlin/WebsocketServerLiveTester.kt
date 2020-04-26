import java.io.BufferedReader
import java.io.InputStreamReader

class WebsocketServerLiveTester

private val server = WebsocketServer()

private val client = WebsocketClient()

fun main() {
    server.start()
    println("Type \"exit\" to stop program...")
    client.connect(Handshake.Client(
        host = "127.0.0.1:8082",
        path = "/",
        key = "PzVereJlM3LETcUrDTfkEQ=="
    ).build())
    val isr = InputStreamReader(System.`in`)
    val bufferedReader = BufferedReader(isr)
    var line = bufferedReader.readLine()
    while (line != "exit") {
        line = bufferedReader.readLine()
    }
    server.stop()
}
