package frame.writer

import Handshake
import java.io.IOException
import java.io.OutputStream
import exception.HandshakeException
import frame.OpCode.*
import exception.InvalidFrameException
import exception.WebsocketException
import exception.WebsocketIOException
import frame.Frame
import frame.OpCode
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.xor

class FrameOutputStreamWriter(private val output: OutputStream) : FrameWriter {

    @Throws(WebsocketException::class)
    override fun write(frame: Frame) {
        when (frame.code) {
            TEXT -> writeData(frame)
            BINARY -> writeData(frame)
            CLOSE -> writeClose(frame)
            PING -> writeControl(frame)
            PONG -> writeControl(frame)
            CONTINUATION -> throw InvalidFrameException(
                "Cannot write a continuation frame; Continuation " +
                "frames must be attached to a data frame."
            )
        }
    }

    @Throws(WebsocketException::class)
    override fun write(handshake: Handshake) {
        try {
            output.write(handshake.toByteArray())
        } catch (ex: IOException) {
            throw HandshakeException(
                "Handshake could not be complete.",
                ex
            )
        }
    }


    @Throws(WebsocketException::class)
    override fun writeHandshake(key: String) {
//        try {
//            output.write(("HTTP/1.1 101 Switching Protocols\r\n").toByteArray())
//            output.write(("Upgrade: websocket\r\n").toByteArray())
//            output.write(("Connection: Upgrade\r\n").toByteArray())
//            output.write(("Sec-WebSocket-Accept: " + key.toAcceptanceKey()).toByteArray())
//            output.write(("\r\n\r\n").toByteArray())
//        } catch (ex: IOException) {
//            throw HandshakeException(
//                "Handshake could not be complete.",
//                ex
//            )
//        }
    }

    @Throws(WebsocketException::class)
    private fun writeData(frame: Frame) {
        var currentFrame = dummyFrame(frame)
        while (currentFrame.next != null) {
            if (currentFrame.isFin) {
                break
            }

            currentFrame = currentFrame.next!!

            if (currentFrame.isMasked) {
                val key = Random().nextInt()
                output.writeMaskedPayload(frame, key)
            } else {
                output.writePayload(currentFrame)
            }
        }
    }

    @Throws(WebsocketException::class)
    private fun writeControl(frame: Frame) {
        if (frame.next != null) {
            throw InvalidFrameException("A control frame cannot be fragmented.")
        } else if (frame.isMasked) {
            val key = Random().nextInt()
            output.writeMaskedPayload(frame, key)
        } else {
            output.writePayload(frame)
        }
    }

    @Synchronized
    @Throws(WebsocketException::class)
    private fun writeClose(frame: Frame) {
        try {
            writeControl(frame)
            output.close()
        } catch (ex: IOException) {
            throw WebsocketIOException(ex)
        }
    }

    companion object {
//        /**
//         * Required to create a magic string and shake hands with client.
//         */
//        private const val MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

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

        /** Convert Short to ByteArray. */
        private fun Short.toByteArray(): ByteArray = toByteArray(2)

        /** Convert Int to ByteArray. */
        private fun Int.toByteArray(): ByteArray = toByteArray(4)

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

//        /**
//         * Generates acceptance key to be sent back to client when performing handshake.
//         * @return The acceptance key.
//         * @throws WebsocketException Thrown when there is an error with the SHA-1 hash function result.
//         * @see <a href="https://tools.ietf.org/html/rfc6455#section-4.2.2">RFC 6455, Section 4.2.2 (Sending the Server's Opening Handshake)</a>
//         */
//        @Throws(HandshakeException::class)
//        private fun String.toAcceptanceKey(): String {
//            try {
//                val message: MessageDigest = MessageDigest.getInstance("SHA-1")
//                val magicString: String = this + MAGIC_KEY
//                message.update(magicString.toByteArray(), 0, magicString.length)
//                // TODO create custom encoder compatible with android and java
//                return Base64.getEncoder().encodeToString(message.digest())
//                // Android encoder
////            return Base64.encodeToString(message.digest(), Base64.DEFAULT)
//            } catch (ex: NoSuchAlgorithmException) {
//                throw HandshakeException(
//                    "Could not apply SHA-1 hashing function to key.",
//                    ex
//                )
//            }
//        }

        /** Construct a dummy Frame. Helps creating the singly linked list. */
        private fun dummyFrame(next: Frame) = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = OpCode.CONTINUATION,
            length = 0,
            payload = ByteArrayOutputStream()
        ).also { it.next = next }

        /**
         * Write an un-masked payload.
         */
        private fun OutputStream.writePayload(frame: Frame) {
            val payload: ByteArray = frame.payload.toByteArray()
            when {
                payload.size <= LENGTH_16_MIN -> {
                    write(payload.size)
                    write(payload)
                }
                payload.size <= LENGTH_64_MIN -> {
                    write(LENGTH_16)
                    val lenBytes = (payload.size.toShort()).toByteArray()
                    write(lenBytes)
                    write(payload)
                }
                else -> {
                    write(LENGTH_64)
                    val lenBytes = (payload.size.toLong()).toByteArray()
                    write(lenBytes)
                    write(payload)
                }
            }
        }

        /**
         * Write a masked payload.
         * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.3">RFC 6455, Section 5.3 (Client-to-Server Masking)</a>
         */
        private fun OutputStream.writeMaskedPayload(frame: Frame, key: Int) {
            val payload: ByteArray = frame.payload.toByteArray()
            when {
                payload.size <= LENGTH_16_MIN -> {
                    write(payload.size)
                    write(key)
                    write(payload.applyMask(key))
                }
                payload.size <= LENGTH_64_MIN -> {
                    write(LENGTH_16)
                    val lenBytes = (payload.size.toShort()).toByteArray()
                    write(lenBytes)
                    write(key)
                    write(payload.applyMask(key))
                }
                else -> {
                    write(LENGTH_64)
                    val lenBytes = (payload.size.toLong()).toByteArray()
                    write(lenBytes)
                    write(key)
                    write(payload.applyMask(key))
                }
            }
        }

        /**
         * Masking the provided ByteArray with the xor algorithm declared in
         * RFC 6455.
         *
         *  j                   = i MOD 4
         *  transformed-octet-i = original-octet-i XOR masking-key-octet-j
         *
         * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.3">RFC 6455, Section 5.3 (Client-to-Server Masking)</a>
         */
        private fun ByteArray.applyMask(key: Int): ByteArray {
            val maskingKey = key.toByteArray()
            for (i in indices) {
                this[i] = this[i] xor maskingKey[i % 4]
            }
            return this
        }
    }
}