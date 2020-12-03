package http

import HttpEngine
import message.Message
import router.Router
import session.factory.SessionFactory
import java.net.InetSocketAddress
import java.util.concurrent.*

class ExampleServer(
    host: String,
    port: Int,
    service: ExecutorService
//    routes: Set<Route>
): HttpEngine(InetSocketAddress(host, port), service) {
    override val router: Router
        get() = TODO("Not yet implemented")

    override val sessionFactory: SessionFactory<Message>
        get() = TODO("Not yet implemented")

    override fun start() {
        println("Starting engine...")
        super.start()
        println("Engine running...")
    }
//    private val engine = HttpEngine.Builder(host, service)
//        .addExceptionHandler(ExampleExceptionHandler())
//        .addHttpExceptionHandler(ExampleHttpExceptionInterceptor())
//        .setPort(port)
//        .setRoutes(routes)
//        .build()
//
//    fun start() {
//        println("Starting engine...")
//        engine.start()
//        println("Engine running...")
//    }
//
//    fun stop() {
//        engine.stop()
//    }
}