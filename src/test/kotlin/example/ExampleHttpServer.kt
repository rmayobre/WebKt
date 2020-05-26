package example

import http.HttpEngine
import http.path.Path
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.*

class ExampleHttpServer(
    host: String,
    port: Int,
    service: ExecutorService,
    paths: Set<Path>
) {
    private val engine = HttpEngine.Builder(host, service)
        .setPort(port)
        .setPaths(paths)
        .build()

    fun start() {
        println("Starting engine...")
        engine.start()
        println("Engine running...")
    }

    fun stop() {
        engine.stop()
    }
}

private val threadPool = ThreadPoolExecutor(1,3, 60, TimeUnit.SECONDS, LinkedBlockingDeque(), ThreadFactory { runnable ->
    Thread(runnable).apply {
        setUncaughtExceptionHandler { thread, throwable ->
            println("${thread.name} (Error): ${throwable.message}")
        }
    }
})

fun main() {
    val paths: Set<Path> = mutableSetOf<Path>().apply {
        add(ExampleHttpPath())
    }

    val server = ExampleHttpServer("localhost", 8080, threadPool, paths)
    server.start()

    val inputStreamReader = InputStreamReader(System.`in`)
    val bufferReader = BufferedReader(inputStreamReader)
    var line: String = bufferReader.readLine()
    while (line != "exit") {
        line = bufferReader.readLine()
    }
    server.stop()
}