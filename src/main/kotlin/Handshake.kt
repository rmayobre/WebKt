import http.Headers

data class Handshake(
    private val line: String,
    private val headers: Headers) {

    fun toByteArray(): ByteArray {
        TODO("Build ByteArray.")
    }

    /** Builder class for client-side Handshake request. */
    inner class Client(host: String, key: String) {

        private val headers = mutableMapOf<String, String>()

        init {
            headers["Host"] = host
            headers["Upgrade"] = WEBSOCKET_UPGRADE_TYPE
            headers["Connection"] = WEBSOCKET_CONNECTION_TYPE
            headers["Sec-WebSocket-Key"] = key
            headers["Sec-WebSocket-Version"] = WEBSOCKET_VERSION
        }

        fun addHeader(key: String, value: String) {
            headers[key] = value
        }

        fun build(): Handshake {
            TODO("Build handshake")
        }
    }

    /** Builder class for server-side Handshake response. */
    inner class Server() {

        private val headers = mutableMapOf<String, String>()

        init {
            headers["Upgrade"] = WEBSOCKET_UPGRADE_TYPE
            headers["Connection"] = WEBSOCKET_CONNECTION_TYPE
            headers["Sec-WebSocket-Accept"] = WEBSOCKET_VERSION
        }

        fun addHeader(key: String, value: String) {
            headers[key] = value
        }

        fun build(): Handshake {
            TODO("Build handshake")
        }
    }

    companion object {
        private const val WEBSOCKET_SERVER_STATUS = "HTTP/1.1 101 Switching Protocols"


        private const val WEBSOCKET_VERSION = "13"
        private const val WEBSOCKET_UPGRADE_TYPE = "websocket"
        private const val WEBSOCKET_CONNECTION_TYPE = "Upgrade"
    }
}