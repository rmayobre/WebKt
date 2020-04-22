import java.io.BufferedReader
import java.io.InputStreamReader

class WebsocketServerLiveTester

private val server = WebsocketServer()

fun main() {
    server.start()
    println("Type \"exit\" to stop program...")
    val isr = InputStreamReader(System.`in`)
    val bufferedReader = BufferedReader(isr)
    var line = bufferedReader.readLine()
    while (line != "exit") {
        line = bufferedReader.readLine()
    }
    server.stop()
}
