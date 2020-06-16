package example

import http.HttpEngine
import http.route.Route
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.*

class ExampleServer(
    host: String,
    port: Int,
    service: ExecutorService,
    routes: Set<Route>
) {
    private val engine = HttpEngine.Builder(host, service)
        .addExceptionHandler(ExampleExceptionHandler())
        .addHttpExceptionHandler(ExampleHttpExceptionInterceptor())
        .setPort(port)
        .setRoutes(routes)
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