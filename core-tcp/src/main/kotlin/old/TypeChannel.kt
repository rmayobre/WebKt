package old

import java.io.IOException
import java.nio.channels.Channel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * A channel that reads and writes a generic data type.
 * @param T The type of data the Channel will read and write.
 */
@Deprecated("remove")
interface TypeChannel<T> : Channel {
    /**
     * Read until a data object is returned.
     * @return The [T] type object
     * @throws IOException when a socket cannot be read.
     */
    @Throws(IOException::class)
    fun read(): T

    /**
     * Read a data object with a provided time limit. If it takes longer than
     * the provided time limit declared in the parameters, the function will
     * throw a [TimeoutException].
     * @param time A number to quantify how long the read can last.
     * @param unit A unit of measurement for [time]
     * @return The [T] type object
     * @throws IOException when a socket cannot be read.
     * @throws TimeoutException when read function takes longer than the limit set.
     */
    @Throws(
        IOException::class,
        TimeoutException::class)
    fun read(time: Int, unit: TimeUnit): T

    /**
     * Write data into the Channel's socket.
     * @param data The data to be written into socket connection.
     * @return The size of data sent.
     * @throws IOException when a socket cannot be written to.
     */
    @Throws(IOException::class)
    fun write(data: T): Int
}