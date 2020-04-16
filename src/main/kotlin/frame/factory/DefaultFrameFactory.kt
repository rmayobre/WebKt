package frame.factory

import ClosureCode
import frame.Frame
import frame.OpCode
import java.io.ByteArrayOutputStream

class DefaultFrameFactory(
    private val sizeLimit: Int = 2048
) : FrameFactory {
    override fun binary(data: ByteArray): Frame {
        if (data.size > sizeLimit) {
            val frame = Frame(
                isFin = false,
                rsv1 = false,
                rsv2 = false,
                rsv3 = false,
                isMasked = false,
                code = OpCode.BINARY,
                length = sizeLimit,
                payload = ByteArrayOutputStream()
            )
            val arrays = mutableListOf<ByteArray>()
        } else {
            val frame = Frame(
                isFin = true,
                rsv1 = false,
                rsv2 = false,
                rsv3 = false,
                isMasked = false,
                code = OpCode.BINARY,
                length = sizeLimit,
                payload = ByteArrayOutputStream()
            )
        }
        TODO("Not yet implemented")
    }

    override fun text(data: String): Frame {
        TODO("Not yet implemented")
    }

    override fun ping(data: ByteArray?): Frame {
        TODO("Not yet implemented")
    }

    override fun pong(data: ByteArray?): Frame {
        TODO("Not yet implemented")
    }

    override fun close(code: ClosureCode?): Frame {
        TODO("Not yet implemented")
    }

    private fun Frame.applyPayload(data: Array<ByteArray>): Frame {
        val head = applyPayload(data[0]) // Apply payload to first frame.
        val tail: Frame? = null
        for (i in 1 until data.size) {

        }


        return head
    }

    private fun Frame.applyPayload(data: ByteArray?): Frame {
        data.let { payload.write(it) }
        return this
    }
//
//    private fun createDataFrame(data: ByteArray): Frame {
//
//    }
//
//    private fun createControlFrame(data: ByteArray): Frame {
//        TODO("Not yet implemented")
//    }
//
//    private fun createFrame()
}