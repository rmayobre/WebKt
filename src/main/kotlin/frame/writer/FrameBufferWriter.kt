package frame.writer

import frame.Frame
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 *
 */
class FrameBufferWriter(
    private val channel: SocketChannel,
    private val buffer: ByteBuffer
) : FrameWriter {
    override fun write(frame: Frame) {
        TODO("not implemented")
    }

    override fun writeHandshake(key: String) {
        TODO("Not yet implemented")
    }
}