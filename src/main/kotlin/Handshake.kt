import exception.HandshakeException
import exception.WebsocketException
import frame.writer.FrameOutputStreamWriter
import http.HttpStatus
import http.Method
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

data class Handshake(
    private val line: String,
    private val headers: Map<String, String>
) {

    private constructor(method: Method, path: String, version: String, headers: Map<String, String>):
            this("${method.name} $path $version", headers)

    private constructor(version: String, status: HttpStatus, headers: Map<String, String>):
            this("$version ${status.code} ${status.message}", headers)

    fun toByteArray(): ByteArray {
        val builder = StringBuilder("$line\r\n")
        headers.forEach { (key, value) ->
            builder.append("$key: $value\r\n")
        }
        builder.append("\r\n")
        return builder.toString().toByteArray()
    }

    /** Builder class for client-side Handshake request. */
    inner class Client(host: String, private val path: String, key: String) {

        private val headers = mutableMapOf<String, String>()

        private var method: Method = Method.GET

        private var version: String = "HTTP/1.1"

        init {
            headers["Host"] = host
            headers["Upgrade"] = WEBSOCKET_UPGRADE_TYPE
            headers["Connection"] = WEBSOCKET_CONNECTION_TYPE
            headers["Sec-WebSocket-Key"] = key
            headers["Sec-WebSocket-Version"] = WEBSOCKET_VERSION
        }

        fun setMethod(method: Method) = apply { this.method = method }

        fun setVersion(version: String) = apply { this.version = version }

        fun addHeader(key: String, value: String) = apply { headers[key] = value }

        fun build(): Handshake = Handshake(method, path, version, headers)
    }

    /** Builder class for server-side Handshake response. */
    inner class Server(key: String) {

        private val headers = mutableMapOf<String, String>()

        private var status: HttpStatus = HttpStatus.SWITCH_PROTOCOL

        private var version: String = "HTTP/1.1"

        init {
            headers["Upgrade"] = WEBSOCKET_UPGRADE_TYPE
            headers["Connection"] = WEBSOCKET_CONNECTION_TYPE
            headers["Sec-WebSocket-Accept"] = key.toAcceptanceKey()
        }

        fun setStatus(status: HttpStatus) = apply { this.status = status }

        fun setVersion(version: String) = apply { this.version = version }

        fun addHeader(key: String, value: String) = apply { headers[key] = value }

        fun build(): Handshake = Handshake(version, status, headers)
    }

    companion object {
        /**
         * Required to create a magic string and shake hands with client.
         */
        private const val MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

        private const val WEBSOCKET_VERSION = "13"

        private const val WEBSOCKET_UPGRADE_TYPE = "websocket"

        private const val WEBSOCKET_CONNECTION_TYPE = "Upgrade"

        /**
         * Generates acceptance key to be sent back to client when performing handshake.
         * @return The acceptance key.
         * @throws WebsocketException Thrown when there is an error with the SHA-1 hash function result.
         * @see <a href="https://tools.ietf.org/html/rfc6455#section-4.2.2">RFC 6455, Section 4.2.2 (Sending the Server's Opening Handshake)</a>
         */
        @Throws(HandshakeException::class)
        private fun String.toAcceptanceKey(): String {
            try {
                val message: MessageDigest = MessageDigest.getInstance("SHA-1")
                val magicString: String = this + MAGIC_KEY
                message.update(magicString.toByteArray(), 0, magicString.length)
                // TODO create custom encoder compatible with android and java
                return Base64.getEncoder().encodeToString(message.digest())
                // Android encoder
//            return Base64.encodeToString(message.digest(), Base64.DEFAULT)
            } catch (ex: NoSuchAlgorithmException) {
                throw HandshakeException(
                    "Could not apply SHA-1 hashing function to key.",
                    ex
                )
            }
        }
    }
}