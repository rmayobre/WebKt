package frame

import exception.InvalidFrameException
import java.io.ByteArrayOutputStream

data class Frame(
    /** Is this the final fragment? */
    val isFin: Boolean,
    val rsv1: Boolean,
    val rsv2: Boolean,
    val rsv3: Boolean,
    /** Is the fragment masked? */
    val isMasked: Boolean,
    /** frame.Fragment;s [OpCode] */
    val code: OpCode,
    /** Length of data in fragment. */
    var length: Int,
    /** frame.Fragment's data. */
    val payload: ByteArrayOutputStream // TODO change to ByteBuffer

) {
    var next: Frame? = null
        set(value) {
            if (isFin) {
                throw InvalidFrameException(
                    "Final frame fragment cannot point to additional frame.")
            }
            field = value
        }

    val data: ByteArray
        get() = if (isFin) {
            payload.toByteArray()
        } else {
            next?.let { payload.write(it.payload.toByteArray()) }
            payload.toByteArray()
        }

    val isDataFrame: Boolean = code.isData

    val isControlFrame: Boolean = code.isControl

    /**
     * Read first and second byte of a stream from an endpoint into a fragment.
     * @param first - First byte from stream.
     * @param second - Second byte from stream.
     * @throws InvalidFrameException
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.2">RFC 6455, Section 5.2 (Base Framing Protocol)</a>
     */
    @Throws(InvalidFrameException::class)
    constructor(first: Int, second: Int): this(
        isFin = first and MASK_FINAL != 0,
        rsv1 = first and MASK_RSV1 != 0,
        rsv2 = first and MASK_RSV2 != 0,
        rsv3 = first and MASK_RSV3 != 0,
        isMasked = second and MASK != 0,
        code = OpCode.find(first and MASK_OPCODE),
        length = second and 0x7F,
        payload = ByteArrayOutputStream())

    init {
        if (isControlFrame) {
            if (!isFin)
                throw InvalidFrameException("A control frame must be final; No fragmentation.")
            if (rsv1)
                throw InvalidFrameException("A control frame cannot have rsv1 set to true")
            if (rsv2)
                throw InvalidFrameException("A control frame cannot have rsv2 set to true")
            if (rsv3)
                throw InvalidFrameException("A control frame cannot have rsv3 set to true")
        }
    }

    // TODO clean up constants
    companion object {
        /** Binary mask to extract the masking flag bit of a WebSocket frame. */
        private const val MASK = 0x80

        /** Binary mask to extract the final fragment flag bit of a WebSocket frame. */
        private const val MASK_FINAL = 0x80

        /** Binary mask to extract RSV1 bit of a WebSocket frame. */
        private const val MASK_RSV1 = 0x40

        /** Binary mask to extract RSV2 bit of a WebSocket frame. */
        private const val MASK_RSV2 = 0x20

        /** Binary mask to extract RSV3 bit of a WebSocket frame. */
        private const val MASK_RSV3 = 0x10

        /** Binary mask to extract the opcode bits of a WebSocket frame. */
        private const val MASK_OPCODE = 0x0F
    }
}