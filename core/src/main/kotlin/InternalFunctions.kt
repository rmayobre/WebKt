import java.nio.ByteBuffer
import javax.net.ssl.SSLSession
import kotlin.math.min

/**
 * Compares `sessionProposedCapacity` with buffer's capacity. If buffer's capacity is smaller,
 * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
 * with capacity twice the size of the initial one.
 *
 * @param this - the buffer to be enlarged.
 * @param size - the minimum size of the new buffer, proposed by [SSLSession].
 * @return A new buffer with a larger allocated capacity.
 */
internal fun ByteBuffer.increaseBufferSizeTo(size: Int): ByteBuffer =
    if (size > capacity()) {
        ByteBuffer.allocate(size)
    } else {
        ByteBuffer.allocate(capacity() * 2)
    }


/**
 * Copy bytes from "this" ByteBuffer to the designated "buffer" ByteBuffer.
 * @param buffer The designated buffer for all bytes to move to.
 * @return number of bytes copied to the other buffer.
 */
internal fun ByteBuffer.copyBytesTo(buffer: ByteBuffer): Int =
    if (remaining() > buffer.remaining()) {
        val limit = min(remaining(), buffer.remaining())
        limit(limit)
        buffer.put(this)
        limit
    } else {
        buffer.put(this)
        remaining()
    }