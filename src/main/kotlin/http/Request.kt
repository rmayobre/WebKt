package http

data class Request(
    val path: String,
    val method: Method,
    val version: String,
    private val headers: Map<String, String>
) {

    val isWebSocketUpgrade: Boolean =
        headers["Upgrade"] == "Websocket"
            && headers["Connection"] == "Upgrade"
            && headers["Sec-WebSocket-Version"] == "13"

    val webSocketKey: String?
        get() = headers["Sec-WebSocket-Key"]

    fun getHeader(key: String): String? = headers[key]
}