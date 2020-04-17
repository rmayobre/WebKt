package frame.reader.factory

import frame.reader.FrameBufferReader
import frame.reader.FrameReader
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class FrameBufferReaderFactory(private val bufferSize: Int) :
    FrameReaderFactory {

    init {
        if (bufferSize < 8) {
            TODO("Throw and exception because you cannot read the integer values.")
        }
    }

    override fun create(channel: SocketChannel): FrameReader {
        return FrameBufferReader(channel, ByteBuffer.allocate(bufferSize))
    }
}