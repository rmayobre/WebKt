package frame.factory

import ClosureCode
import frame.Frame
import frame.OpCode
import java.io.ByteArrayOutputStream

class DefaultFrameFactory(
    /** Apply mask to each frame? */
    private val applyMask: Boolean,
    /** Byte limit for each frame before it requires fragmentation. */
    private val fragmentSize: Int = DEFAULT_FRAGMENT_SIZE
) : FrameFactory {
    override fun binary(data: ByteArray): Frame =
        data.toDataFrame(fragmentSize, OpCode.BINARY, applyMask)

    override fun text(msg: String): Frame =
        msg.toByteArray().toDataFrame(fragmentSize, OpCode.TEXT, applyMask)

    override fun ping(data: ByteArray?): Frame =
        data.toControlFrame(OpCode.PING, applyMask)

    override fun pong(data: ByteArray?): Frame =
        data.toControlFrame(OpCode.PONG, applyMask)

    override fun close(code: ClosureCode): Frame =
        code.bytes.toControlFrame(OpCode.CLOSE, applyMask)

    companion object {

        private const val DEFAULT_FRAGMENT_SIZE = 8196

        private fun ByteArray?.toControlFrame(code: OpCode, isMasked: Boolean): Frame = Frame(
            isFin = true,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = isMasked,
            code = code,
            length = this?.size ?: 0,
            payload = ByteArrayOutputStream()
        ).also { this?.let { data -> it.payload.write(data) } }

        private fun ByteArray.toDataFrame(limit: Int, code: OpCode, isMasked: Boolean): Frame {
            if (size > limit) {
                var start = limit
                val head = toFragmentedFrame(limit, code, isMasked)
                var current = head
                while (start < size) {
                    val remaining = size - start
                    current.next = when {
                        remaining < limit -> toFinalFrame(start, remaining, code, isMasked)
                        else -> toContinuationFrame(start, limit, isMasked)
                    }
                    current = current.next!!
                    start += limit
                }
                return head
            } else {
                return toFinalFrame(limit, code, isMasked)
            }
        }

        private fun ByteArray.toFinalFrame(length: Int, code: OpCode, isMasked: Boolean): Frame = Frame(
            isFin = true,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = isMasked,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(this) }

        /** Build the final frame. */
        private fun ByteArray.toFinalFrame(start: Int, length: Int, code: OpCode, isMasked: Boolean): Frame = Frame(
            isFin = true,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = isMasked,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(start, start + length - 1)) }

        /** Build a continuation frame. */
        private fun ByteArray.toContinuationFrame(start: Int, length: Int, isMasked: Boolean): Frame = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = isMasked,
            code = OpCode.CONTINUATION,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(start, start + length - 1)) }

        /** Build a fragmented frame. */
        private fun ByteArray.toFragmentedFrame(length: Int, code: OpCode, isMasked: Boolean): Frame = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = isMasked,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(0, length - 1)) }
    }
}