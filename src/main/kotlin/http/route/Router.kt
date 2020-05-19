package http.route

import http.Method.*
import http.message.Request
import http.message.Response

open class Router(path: String, configuration: (RouteMap.() -> Unit)) {
    private val routes = RouteMap().run { configuration() }

    fun service(request: Request): Response = when(request.method) {
        GET -> TODO()
        PUT -> TODO()
        POST -> TODO()
        DELETE -> TODO()
        HEAD -> TODO()
        OPTIONS -> TODO()
        TRACE -> TODO()
        CONNECT -> TODO()
    }
}