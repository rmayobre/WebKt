package http

import java.net.URI

data class RestHeader(
    val uri: URI,
    val path: String,
    val method: Method,
    private val headers: Map<String, String>
) {
    fun getHeader(key: String): String? = headers[key]
}
/*
data class HandshakeData(private val header: RestHeader) {
    val uri: URI
        get() = header.uri

    val path: String
        get() = header.path

    val method: Method
        get() = header.method

    val isWebSocketUpgrade: Boolean
        get() = header.getHeader("Upgrade") == "Websocket"
                && header.getHeader("Connection") == "Upgrade"
                && header.getHeader("Sec-WebSocket-Version") == "13"

    val webSocketKey: String?
        get() = header.getHeader("Sec-WebSocket-Key")
}
 */