package frame.factory

import ClosureCode
import frame.Frame
import frame.OpCode
import java.io.ByteArrayOutputStream

class DefaultFrameFactory(
    /** Byte limit for each frame before it requires fragmentation. */
    private val sizeLimit: Int = 2048
) : FrameFactory {
    override fun binary(data: ByteArray): Frame =
        data.toDataFrame(sizeLimit, OpCode.BINARY)

    override fun text(data: String): Frame =
        data.toByteArray().toDataFrame(sizeLimit, OpCode.TEXT)

    override fun ping(data: ByteArray?): Frame {
        TODO("Not yet implemented")
    }

    override fun pong(data: ByteArray?): Frame {
        TODO("Not yet implemented")
    }

    override fun close(code: ClosureCode?): Frame {
        TODO("Not yet implemented")
    }

    companion object {

        private fun ClosureCode.toByteArray(): ByteArray {
            TODO("Convert ClosureCode into the proper byte array for a closing frame.")
        }

        private fun ByteArray?.toControlFrame(): Frame {
            TODO("Implement conversion to control FINAL frame")
        }

        private fun ByteArray.toDataFrame(limit: Int, code: OpCode): Frame {
            if (size > limit) {
                var start = limit
                val head = toFragmentedFrame(limit, code)
                var current = head
                while (start < size) {
                    val remaining = size - start
                    current.next = when {
                        remaining < limit -> toFinalFrame(start, remaining, code)
                        else -> toContinuationFrame(start, limit)
                    }
                    current = current.next!!
                    start += limit
                }
                return head
            } else {
                return toFinalFrame(limit, code)
            }
        }

        private fun ByteArray.toFinalFrame(length: Int, code: OpCode): Frame = Frame(
            isFin = true,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(this) }

        /** Build the final frame. */
        private fun ByteArray.toFinalFrame(start: Int, length: Int, code: OpCode): Frame = Frame(
            isFin = true,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(start, start + length - 1)) }

        /** Build a continuation frame. */
        private fun ByteArray.toContinuationFrame(start: Int, length: Int): Frame = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = OpCode.CONTINUATION,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(start, start + length - 1)) }

        /** Build a fragmented frame. */
        private fun ByteArray.toFragmentedFrame(length: Int, code: OpCode): Frame = Frame(
            isFin = false,
            rsv1 = false,
            rsv2 = false,
            rsv3 = false,
            isMasked = false,
            code = code,
            length = length,
            payload = ByteArrayOutputStream()
        ).also { it.payload.write(copyOfRange(0, length - 1)) }
    }
}