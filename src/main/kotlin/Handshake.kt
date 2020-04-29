import exception.HandshakeException
import exception.WebsocketException
import http.Status
import http.Method
import http.message.Message
import http.message.Request
import http.message.Response
import java.security.MessageDigest

data class Handshake(private val message: Message) : Message by message {

    fun toByteArray(): ByteArray {
        val builder = StringBuilder("$line\r\n")
        headers.forEach { (key, value) ->
            builder.append("$key: $value\r\n")
        }
        builder.append("\r\n")
        return builder.toString().toByteArray()
    }

    /** Builder class for client-side Handshake request. */
    class Client(host: String, path: String, key: String) {

        private val builder = Request.Builder(Method.GET)
            .setPath(path)
            .setVersion("HTTP/1.1")
            .addHeader("Host", host)
            .addHeader("Upgrade", WEBSOCKET_UPGRADE_TYPE)
            .addHeader("Connection", WEBSOCKET_CONNECTION_TYPE)
            .addHeader("Sec-WebSocket-Key", key)
            .addHeader("Sec-WebSocket-Version", WEBSOCKET_VERSION)

        fun setVersion(version: String) = apply {
            builder.setVersion(version)
        }

        fun addHeader(key: String, value: String) = apply {
            builder.addHeader(key, value)
        }

        fun build() = Handshake(builder.build())

        override fun toString(): String = builder.toString()
    }

    /** Builder class for server-side Handshake response. */
    class Server(key: String) {

        private val builder = Response.Builder(Status.SWITCH_PROTOCOL)
            .setVersion("HTTP/1.1")
            .addHeader("Upgrade", WEBSOCKET_UPGRADE_TYPE)
            .addHeader("Connection", WEBSOCKET_CONNECTION_TYPE)
            .addHeader("Sec-WebSocket-Accept", key.toAcceptanceKey())

        fun setVersion(version: String) = apply {
            builder.setVersion(version)
        }

        fun addHeader(key: String, value: String) = apply {
            builder.addHeader(key, value)
        }

        fun build() = Handshake(builder.build())

        override fun toString(): String = builder.toString()
    }

    companion object {
        /**
         * Required to create a magic string and shake hands with client.
         */
        private const val MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

        private const val WEBSOCKET_VERSION = "13"

        private const val WEBSOCKET_UPGRADE_TYPE = "websocket"

        private const val WEBSOCKET_CONNECTION_TYPE = "Upgrade"

        fun default(key: String): Handshake =
            Server(key).build()

        fun default(host: String, path: String, key: String) =
            Client(host, path, key).build()

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
                return message.digest().toBase64String()
            } catch (ex: Exception) {
                throw HandshakeException(
                    "Could not apply SHA-1 hashing function to key.",
                    ex
                )
            }
        }
    }
}