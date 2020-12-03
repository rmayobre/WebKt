package websocket.frame.reader

import websocket.LargeFrameException
import websocket.frame.Frame
import websocket.MissingMaskFragmentException
import websocket.WebsocketIOException
import websocket.frame.OpCode
import java.io.*
import kotlin.experimental.and
import kotlin.experimental.xor

class FrameInputStreamReader(
    private val input: InputStream,
    /**
     * The limit for how large a websocket.frame can be. This includes frames that
     * are fragmented. The reason is to prevent an overload on the system.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-10.4">RFC 6455, Section 10.4 (Defined Status Codes)</a>
     */
    private val maxFrameSize: Int
) : FrameReader {

    override fun read(requiresMask: Boolean): Frame {
        var totalSize = 0
        val head = dummyFrame()
        try {
            var last: Frame = head
            do {
                val frame = read(input.read(), input.read(), requiresMask)
                totalSize += frame.length
                if (totalSize > maxFrameSize) {
                    throw LargeFrameException(maxFrameSize)
                }
                last.next = frame
                last = frame
            } while (!last.isFin)
        } catch (ex: IOException) {
            throw WebsocketIOException(ex)
        }
        return head.next!!
    }

    @Throws(MissingMaskFragmentException::class, IOException::class)
    private fun read(first: Int, second: Int, requiresMask: Boolean): Frame {
        val frame = Frame(first, second)
        if (frame.length == PAYLOAD_LENGTH_16) {
            frame.extendedPayloadLength(TWO_BYTE_FRAME_LENGTH)
        } else if (frame.length == PAYLOAD_LENGTH_64) {
            frame.extendedPayloadLength(EIGHT_BYTE_FRAME_LENGTH)
        }
        if (requiresMask) {
            if (frame.isMasked) {
                frame.readFromPayloadWithMask()
            } else {
                throw MissingMaskFragmentException()
            }
        } else {
            frame.readFromPayload()
        }
        return frame
    }

    private fun Frame.extendedPayloadLength(size: Int) {
        length = 0
        val payload = ByteArray(size)
        input.read(payload)

        payload.forEach { load ->
            length = (length shl 8) + (load and 0xFF.toByte())
        }
    }

    private fun Frame.readFromPayloadWithMask() {
        val maskingKey = ByteArray(MASK_BYTES)
        input.read(maskingKey)

        readFromPayload { payload, position -> payload xor maskingKey[position % 4] }
    }


    private fun Frame.readFromPayload() = readFromPayload { payload, _ -> payload }

    private fun Frame.readFromPayload(dataBlock: (payload: Byte, position: Int) -> Byte) {
        val payload = ByteArray(length)
        input.read(payload)

        for (i in 0 until length) {
            this.payload.write(dataBlock(payload[i], i).toInt())
        }
    }

    @Throws(IOException::class)
    @Synchronized override fun close() = input.close()

    companion object {

        /** Number of masking bytes provided from websocket.client. */
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
            payload = ByteArrayOutputStream())
    }
}