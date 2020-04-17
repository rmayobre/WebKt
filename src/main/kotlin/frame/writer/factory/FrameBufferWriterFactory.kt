package frame.writer.factory

import frame.writer.FrameBufferWriter
import frame.writer.FrameWriter
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class FrameBufferWriterFactory(private val bufferSize: Int) :
    FrameWriterFactory {

    init {
        if (bufferSize < 8) {
            TODO("Throw and exception because you cannot read the integer values.")
        }
    }

    override fun create(channel: SocketChannel): FrameWriter {
        return FrameBufferWriter(channel, ByteBuffer.allocate(bufferSize))
    }
}