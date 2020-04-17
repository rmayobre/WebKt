package frame.reader

import exception.InvalidFrameException
import exception.MissingMaskFragmentException
import exception.WebsocketIOException
import frame.Frame
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.experimental.and
import kotlin.experimental.xor

// TODO This is not finished!
class FrameBufferReader(
    private val channel: SocketChannel,
    /** Buffer size cannot be smaller than two integers (e.g 2(Integers) * 4(Bytes) = 8(Minimum Size)) */
    private val buffer: ByteBuffer,
    /**
     * The limit for how large a frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val frameSizeLimit: Int
) : FrameReader {

    override fun read(requiresMask: Boolean): Frame {
        val head = Frame()
        try {
            var last: Frame = head
            do {
                // Build a Frame from first two 32 bit values.
                buffer.flip()
                if (channel.read(buffer) != -1) {
                    throw InvalidFrameException("Could not read frame from channel.")
                }
                val first = buffer.int
                val second = buffer.int
                val frame = Frame(first, second)

                frame.buildPayload(requiresMask)

                while (channel.read(buffer) != -1) {
                    buffer.flip()

                    buffer.clear()
                }

                last.next = frame
                last = frame
            } while (!last.isFin)
        } catch (ex: IOException) {
            throw WebsocketIOException(ex)
        }
        return head.next ?: throw InvalidFrameException("")
    }

    @Throws(MissingMaskFragmentException::class)
    private fun Frame.buildPayload(requiresMask: Boolean) {
        while (channel.read(buffer) != -1) {
            buffer.flip()

            while (buffer.hasRemaining()) {

            }

            buffer.clear()
        }



        if (length == PAYLOAD_LENGTH_16) {
            extendedPayloadLength(TWO_BYTE_FRAME)
        } else if (length == PAYLOAD_LENGTH_64) {
            extendedPayloadLength(EIGHT_BYTE_FRAME)
        }
        if (requiresMask) {
            if (isMasked) {
                readFromPayloadWithMask()
            } else {
                throw MissingMaskFragmentException()
            }
        } else {
            if (isMasked) {
                readFromPayloadWithMask()
            } else {
                readFromPayload()
            }
        }
    }

    private fun Frame.extendedPayloadLength(size: Int) {
        length = 0
        for (i in 0 until size) {
            length = (length shl 8) + (buffer.get() and 0xFF.toByte())
        }
    }

    private fun Frame.readFromPayloadWithMask() {
        val maskingKey = ByteArray(MASK_BYTES)
        for (i in 0 until MASK_BYTES) {
            maskingKey[i] = buffer.get()
        }

        readFromPayload { payload, position -> payload xor maskingKey[position % 4] }
    }


    private fun Frame.readFromPayload() = readFromPayload { payload, _ -> payload }

    private fun Frame.readFromPayload(dataBlock: (payload: Byte, position: Int) -> Byte) {
        for (i in 0 until length) {
            payload.write(dataBlock(buffer.get(), i).toInt())
        }
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object {
        /** Number of masking bytes provided from client. */
        private const val MASK_BYTES = 0x4
        /** Number of bits required to shift octet 1 into the lowest 8 bits. */
        private const val OCTET_ONE = 8
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

        private const val TWO_BYTE_FRAME = 0x2

        private const val EIGHT_BYTE_FRAME = 0x8
    }
}