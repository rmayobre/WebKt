package frame.writer

import java.io.IOException
import java.io.OutputStream
import exception.HandshakeException
import frame.OpCode.*
import exception.InvalidFrameException
import exception.WebsocketException
import exception.WebsocketIOException
import frame.Frame
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class FrameOutputStreamWriter(private val output: OutputStream) : FrameWriter {

    @Throws(WebsocketException::class)
    override fun write(frame: Frame) {
        when (frame.code) {
            CONTINUATION -> throw InvalidFrameException(
                "Cannot write a continuation frame; Continuation " +
                    "frames must be attached to a data frame.")
            TEXT -> writeData(frame)
            BINARY -> writeData(frame)
            CLOSE -> writeClose(frame)
            PING -> ping(frame)
            PONG -> pong(frame)
        }
    }

    @Throws(WebsocketException::class)
    override fun writeHandshake(key: String) {
        try {
            output.write(("HTTP/1.1 101 Switching Protocols\r\n").toByteArray())
            output.write(("Upgrade: websocket\r\n").toByteArray())
            output.write(("Connection: Upgrade\r\n").toByteArray())
            output.write(("Sec-WebSocket-Accept: " + key.toAcceptanceKey()).toByteArray())
            output.write(("\r\n\r\n").toByteArray())
        } catch (ex: IOException) {
            throw HandshakeException(
                "Handshake could not be complete.",
                ex
            )
        }
    }

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

    @Throws(WebsocketException::class)
    private fun writeData(frame: Frame) {
        var currentFrame = Frame().also { it.next = frame }
        while (currentFrame.next != null) {
            if (currentFrame.isFin) {
                break
            }

            currentFrame = currentFrame.next!!
            val payload: ByteArray = frame.payload.toByteArray()
            when {
                payload.size <= LENGTH_16_MIN -> {
                    output.write(payload.size)
                    output.write(payload)
                }
                payload.size <= LENGTH_64_MIN -> {
                    output.write(LENGTH_16)
                    val lenBytes = (payload.size.toShort()).toByteArray()
                    output.write(lenBytes)
                    output.write(payload)
                }
                else -> {
                    output.write(LENGTH_64)
                    val lenBytes = (payload.size.toLong()).toByteArray()
                    output.write(lenBytes)
                    output.write(payload)
                }
            }
        }
    }

    @Synchronized
    @Throws(WebsocketException::class)
    private fun writeClose(frame: Frame) {
        val status = ByteBuffer.wrap(frame.payload.toByteArray()).int
        try {
            output.write(byteArrayOf(
                OPCODE_CLOSE.toByte(), 0x02,
                ((status and MASK_LOW_WORD_HIGH_BYTE) shr OCTET_ONE).toByte(),
                (status and MASK_LOW_WORD_LOW_BYTE).toByte()))
            output.close()
        } catch (ex: IOException) {
            throw WebsocketIOException(ex)
        }
    }

    @Throws(WebsocketException::class)
    private fun ping(frame: Frame) { TODO("Implement ping functionality") }

    @Throws(WebsocketException::class)
    private fun pong(frame: Frame) { TODO("Implement ping functionality") }

    /** Convert Short to ByteArray. */
    private fun Short.toByteArray(): ByteArray = toByteArray(2)

    /** Convert Long to ByteArray. */
    private fun Long.toByteArray(): ByteArray = toByteArray(8)

    /**
     * Create a ByteArray from the provided Number
     * @param length Byte length of Number.
     */
    private fun Number.toByteArray(length: Int): ByteArray {
        val data = ByteArray(length)
        for (i in 0 until length)
            data[i] = (this.toLong() shr 8 * (length - i - 1) and 0xFF).toByte()
        return data
    }

    companion object {
        /**
         * Required to create a magic string and shake hands with client.
         */
        private const val MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

	    /**
         * Payload length indicating that the payload's true length is a
         * yet-to-be-provided unsigned 16-bit integer.
         */
	    private const val LENGTH_16 = 0x7E

        /**
         * A payload specified with 16 bits must have at least this
         * length in order to be considered valid.
         */
        private const val LENGTH_16_MIN = 0x7D

        /**
         * Payload length indicating that the payload's true length is a
         * yet-to-be-provided unsigned 64-bit integer (MSB = 0).
         */
        private const val LENGTH_64 = 0x7F

        /**
         * A payload specified with 64 bits must have at least this
         * length in order to be considered valid.
         */
        private const val LENGTH_64_MIN = 0xffff

	    /**
         * Binary mask to remove all but the bits of octet 1.
         */
        private const val MASK_LOW_WORD_HIGH_BYTE = 0x0000ff00

        /**
         * Binary mask to remove all but the lowest 8 bits (octet 0).
         */
        private const val MASK_LOW_WORD_LOW_BYTE = 0x000000ff

        /**
         * Number of bits required to shift octet 1 into the lowest 8 bits.
         */
        private const val OCTET_ONE = 8

        /**
         * WebSocketIOTemp defined opcode for a Binary frame. Includes high bit (0x80)
         * to indicate that the frame is the final/complete frame.
         */
        private const val OPCODE_BINARY = 0x82

        /**
         * WebSocketIOTemp defined opcode for a Close frame. Includes high bit (0x80)
         * to indicate that the frame is the final/complete frame.
         */
        private const val OPCODE_CLOSE = 0x88

        /**
         * WebSocketIOTemp defined opcode for a Pong frame. Includes high bit (0x80)
         * to indicate that the frame is the final/complete frame.
         */
        private const val OPCODE_PONG = 0x8A

        /**
         * WebSocketIOTemp defined opcode for a Text frame. Includes high bit (0x80)
         * to indicate that the frame is the final/complete frame.
         */
        private const val OPCODE_TEXT = 0x81

    }
}