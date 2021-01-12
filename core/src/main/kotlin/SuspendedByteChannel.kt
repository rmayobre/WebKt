import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channel
import kotlin.jvm.Throws

interface SuspendedByteChannel : Channel {

    /**
     * A suspended process of reading from a socket connection into a buffer.
     * @param buffer the buffer where data is read into.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    @Throws(IOException::class)
    suspend fun read(buffer: ByteBuffer): Int

    /**
     * A suspended process of writing data into a socket connection.
     * @param buffer the data what will be written into the socket.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    @Throws(IOException::class)
    suspend fun write(buffer: ByteBuffer): Int

}