package http.message

import http.Method

data class Request(
    val path: String,
    val method: Method,
    val version: String,
    override val line: String,
    override val headers: Map<String, String>,
    override val body: String? = null
): Message {

    val isWebSocketUpgrade: Boolean =
        headers["Upgrade"] == "Websocket"
            && headers["Connection"] == "Upgrade"
            && headers["Sec-WebSocket-Version"] == "13"

    val webSocketKey: String?
        get() = headers["Sec-WebSocket-Key"]

    constructor(path: String, method: Method, version: String, headers: MutableMap<String, String>) :
            this(path, method, version, "${method.name} $path $version", headers)

    data class Builder(private val method: Method) {

        private val headers = mutableMapOf<String, String>()

        private var path: String = "/"

        private var version: String = "HTTP/1.1"

        fun setPath(path: String) = apply { this.path = path }

        fun setVersion(version: String) = apply { this.version = version }

        fun addHeader(key: String, value: String) = apply { headers[key] = value }

        fun build(): Request = Request(path, method, version, headers)
    }
}