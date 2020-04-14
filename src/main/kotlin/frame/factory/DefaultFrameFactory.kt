package frame.factory

import ClosureCode
import frame.Frame

class DefaultFrameFactory : FrameFactory {
    override fun binary(data: ByteArray): Frame {
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
}