package http

import java.net.URI

data class Request(
    val uri: URI,
    val path: String,
    val method: Method,
    private val headers: Map<String, String>
) {
    val isWebSocketUpgrade: Boolean
        get() = headers["Upgrade"] == "Websocket"
                && headers["Connection"] == "Upgrade"
                && headers["Sec-WebSocket-Version"] == "13"

    val webSocketKey: String? = headers["Sec-WebSocket-Key"]

    fun getHeader(key: String): String? = headers[key]
}