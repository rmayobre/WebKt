package http

import http.ExampleServer
import route.Route
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.*

class ExampleMain


private val threadPool = ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, LinkedBlockingDeque(), ThreadFactory { runnable ->
    Thread(runnable).apply {
        setUncaughtExceptionHandler { thread, throwable ->
            println("${thread.name} (Uncaught Error): ${throwable.message}")
        }
    }
})


fun main() {
    val routes: Set<Route> = mutableSetOf<Route>().apply {
        add(ExampleHttpRoute())
        add(ExampleWebsocketRoute(Executors.newFixedThreadPool(3)))
    }

    val server = ExampleServer("localhost", 8080, threadPool, routes)
    server.start()

    val inputStreamReader = InputStreamReader(System.`in`)
    val bufferReader = BufferedReader(inputStreamReader)
    var line: String = bufferReader.readLine()
    while (line != "exit") {
        line = bufferReader.readLine()
    }
    server.stop()
}