package frame.reader

import exception.*
import frame.Frame
import frame.OpCode
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.experimental.and
import kotlin.experimental.xor

class FrameBufferReader(
    private val channel: SocketChannel,
    /**
     * The limit for how large a frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val maxFrameSize: Int,
    bufferSize: Int
) : FrameReader {

    private val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

    @Throws(WebsocketException::class)
    override fun read(requiresMask: Boolean): Frame {
        try {
            var totalSize = 0
            val head = dummyFrame()
            var last: Frame = head
            do {
                if (channel.read(buffer) != -1) {
                    throw InvalidFrameException("Could not read frame from channel.")
                }

                buffer.flip()

                val frame = buildFrame()

                totalSize += frame.length
                if (totalSize > maxFrameSize) {
                    throw LargeFrameException(maxFrameSize)
                }

                frame.writePayload(requiresMask)

                last.next = frame
                last = frame
            } while (!last.isFin)

            buffer.clear()

            return head.next ?: throw InvalidFrameException("Failed ot build a frame.")
        } catch (exception: WebsocketException) {
            buffer.clear()
            channel.close()
            throw exception
        }
    }

    /**
     * Read attributes of a frame and builds the Frame object.
     * @throws IOException if it cannot read from channel anymore.
     */
    @Throws(WebsocketIOException::class)
    private fun buildFrame(): Frame {
        try {
            // A frame requires 8 bytes (64 bits) to construct it's object.
            val bytes = ByteBuffer.allocate(8)
            while (bytes.hasRemaining()) {
                if (!buffer.hasRemaining()) {
                    buffer.clear()
                    channel.read(buffer)
                    buffer.flip()
                }
                bytes.put(buffer.get())
            }
            return Frame(bytes.int, bytes.int)
        } catch (ex: IOException) {
            throw WebsocketIOException("Could not read from SocketChannel.", ex)
        }
    }

    private fun Frame.writePayload(requiresMask: Boolean) {
        // Check if the payload has an extend length.
        if (length == PAYLOAD_LENGTH_16) {
            extendedPayloadLength(TWO_BYTE_FRAME_LENGTH)
        } else if (length == PAYLOAD_LENGTH_64) {
            extendedPayloadLength(EIGHT_BYTE_FRAME_LENGTH)
        }

        // Check if a mask is required for the payload, then read.
        if (requiresMask) {
            if (isMasked) {
                readFromPayloadWithMask()
            } else {
                throw MissingMaskFragmentException()
            }
        } else {
            readFromPayload()
        }
    }

    private fun Frame.extendedPayloadLength(size: Int) {
        length = 0
        for (i in 0 until size) {
            if (!buffer.hasRemaining()) {
                buffer.clear()
                channel.read(buffer)
                buffer.flip()
            }
            length = (length shl 8) + (buffer.get() and 0xFF.toByte())
        }
    }

    private fun Frame.readFromPayload() {
        for (i in 0 until length) {
            if (!buffer.hasRemaining()) {
                buffer.clear()
                channel.read(buffer)
                buffer.flip()
            }
            payload.write(buffer.get().toInt())
        }
    }

    private fun Frame.readFromPayloadWithMask() {
        val maskingKey = ByteArray(MASK_BYTES)
        for (i in 0 until MASK_BYTES) {
            if (!buffer.hasRemaining()) {
                buffer.clear()
                channel.read(buffer)
                buffer.flip()
            }
            maskingKey[i] = buffer.get()
        }

        for (i in 0 until length) {
            if (!buffer.hasRemaining()) {
                buffer.clear()
                channel.read(buffer)
                buffer.flip()
            }
            payload.write((buffer.get() xor maskingKey[i % 4]).toInt())
        }
    }

    @Throws(IOException::class)
    @Synchronized override fun close() = channel.socket().getInputStream().close()

    companion object {

        /** Number of masking bytes provided from client. */
        private const val MASK_BYTES = 0x4

        /**
         * Payload length indicating that the payload's true length is a
         * yet-to-be-provided unsigned 16-bit integer.
         */
        private const val PAYLOAD_LENGTH_16 = 0x7E

        /**
         * Payload length indicating that the payload's true length is a
         * yet-to-be-provided unsigned 64-bit integer (MSB = 0).
         */
        private const val PAYLOAD_LENGTH_64 = 0x7F

        /** S */
        private const val TWO_BYTE_FRAME_LENGTH = 0x2

        private const val EIGHT_BYTE_FRAME_LENGTH = 0x8


        /** Construct a dummy Frame. Helps creating the singly linked list. */
        private fun dummyFrame() = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = OpCode.CONTINUATION,
            length = 0,
            payload = ByteArrayOutputStream()
        )
    }
}