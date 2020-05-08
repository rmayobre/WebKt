package websocket

import http.Method
import http.Status
import http.message.Message
import http.message.Request
import http.message.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

data class Handshake(private val message: Message) : Message by message {

    fun toByteArray(): ByteArray {
        val builder = StringBuilder("$line\r\n")
        headers.forEach { (key, value) ->
            builder.append("$key: $value\r\n")
        }
        builder.append("\r\n")
        return builder.toString().toByteArray()
    }

    /** Builder class for websocket.client-side websocket.Handshake request. */
    class Client(host: String) {

        private val builder = Request.Builder(Method.GET)
            .addHeader("Host", host)
            .addHeader("Upgrade", WEBSOCKET_UPGRADE_TYPE)
            .addHeader("Connection", WEBSOCKET_CONNECTION_TYPE)
            .addHeader("Sec-WebSocket-Key", generateKey())
            .addHeader("Sec-WebSocket-Version", WEBSOCKET_VERSION)

        fun setKey(key: String) = apply {
            builder.addHeader("Sec-WebSocket-Key", key)
        }

        fun setPath(path: String) = apply {
            builder.setPath(path)
        }

        fun setVersion(version: String) = apply {
            builder.setVersion(version)
        }

        fun addHeader(key: String, value: String) = apply {
            builder.addHeader(key, value)
        }

        fun build() = Handshake(builder.build())

        override fun toString(): String = builder.toString()
    }

    /** Builder class for websocket.server-side websocket.Handshake response. */
    class Server @Throws(NoSuchAlgorithmException::class) constructor(key: String) {

        private val builder = Response.Builder(Status.SWITCH_PROTOCOL)
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
        private const val MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

        private const val WEBSOCKET_VERSION = "13"

        private const val WEBSOCKET_UPGRADE_TYPE = "websocket"

        private const val WEBSOCKET_CONNECTION_TYPE = "Upgrade"

        fun server(key: String): Handshake = Server(
            key
        ).build()

        fun client(host: String): Handshake = Client(
            host
        ).build()

        /**
         * Generates acceptance key to be sent back to websocket.client when performing handshake.
         * @return The acceptance key.
         * @throws WebsocketException Thrown when there is an error with the SHA-1 hash function result.
         * @see <a href="https://tools.ietf.org/html/rfc6455#section-4.2.2">RFC 6455, Section 4.2.2 (Sending the Server's Opening websocket.Handshake)</a>
         */
        @Throws(NoSuchAlgorithmException::class)
        private fun String.toAcceptanceKey(): String {
            val message: MessageDigest = MessageDigest.getInstance("SHA-1")
            val magicString: String = this + MAGIC_KEY
            message.update(magicString.toByteArray(), 0, magicString.length)
            return message.digest().toBase64String()
        }

        private fun generateKey(): String {
            val random = ByteArray(16)
            Random().nextBytes(random)
            return random.toBase64String()
        }
    }
}