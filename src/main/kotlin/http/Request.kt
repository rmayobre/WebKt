package http

data class Request(private val headers: Headers) {

    constructor(path: String, method: Method, version: String, headers: Map<String, String>):
            this(Headers(path, method, version, headers))

    val isWebSocketUpgrade: Boolean =
        headers.getHeader("Upgrade") == "Websocket"
            && headers.getHeader("Connection") == "Upgrade"
            && headers.getHeader("Sec-WebSocket-Version") == "13"

    val webSocketKey: String?
        get() = headers.getHeader("Sec-WebSocket-Key")

    fun getHeader(key: String): String? = headers.getHeader(key)
}