import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.Channel
import java.util.concurrent.TimeoutException

/**
 *
 */
abstract class ServerEngine<T : Channel>(
    /** Network address the server engine binds to. */
    val address: InetSocketAddress
) {

    /**
     * Determine if Engine is running
     */
    abstract val isRunning: Boolean

    /**
     * Start your engine.
     * @throws IOException thrown if engine could not open sockets.
     */
    @Throws(IOException::class)
    abstract fun start()

    /**
     * Stop the engine.
     */
    @Throws(IOException::class)
    abstract fun stop()

    /**
     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
     * the channel to the appropriate settings during this function call.
     * @throws IOException when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    protected abstract fun onAccept(channel: T): Boolean

    /**
     * A channel is ready to be read by the engine's implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    protected abstract fun onRead(channel: T)

    /**
     * Reports an exception occurred while processing a channel.
     */
    protected abstract fun onException(ex: Exception)
}