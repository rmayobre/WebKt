package websocket.frame.writer

import websocket.applyMask
import java.io.IOException
import java.io.OutputStream
import websocket.InvalidFrameException
import websocket.WebsocketException
import websocket.WebsocketIOException
import websocket.frame.Frame
import websocket.frame.OpCode
import websocket.toByteArray
import java.io.ByteArrayOutputStream
import java.util.*

class FrameOutputStreamWriter(private val output: OutputStream) : FrameWriter {

    @Throws(WebsocketException::class)
    override fun write(frame: Frame) {
        when (frame.code) {
            OpCode.TEXT -> writeData(frame)
            OpCode.BINARY -> writeData(frame)
            OpCode.CLOSE -> writeClose(frame)
            OpCode.PING -> writeControl(frame)
            OpCode.PONG -> writeControl(frame)
            OpCode.CONTINUATION -> throw InvalidFrameException(
                "Cannot write a continuation websocket.frame; Continuation " +
                        "frames must be attached to a data websocket.frame."
            )
        }
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
            throw InvalidFrameException("A control websocket.frame cannot be fragmented.")
        } else if (frame.isMasked) {
            val key = Random().nextInt()
            output.writeMaskedPayload(frame, key)
        } else {
            output.writePayload(frame)
        }
    }

    @Synchronized
    @Throws(WebsocketIOException::class)
    private fun writeClose(frame: Frame) {
        try {
            writeControl(frame)
            output.close()
        } catch (ex: IOException) {
            throw WebsocketIOException(ex)
        }
    }

    companion object {

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

        /** private construct a dummy Frame. Helps creating the singly linked list. */
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
                    val lenBytes = payload.size.toShort().toByteArray()
                    write(lenBytes)
                    write(payload)
                }
                else -> {
                    write(LENGTH_64)
                    val lenBytes = payload.size.toLong().toByteArray()
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
    }
}