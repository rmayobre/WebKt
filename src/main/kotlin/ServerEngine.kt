import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.util.concurrent.TimeUnit

interface ServerEngine {

    val address: InetSocketAddress

    /**
     * Start your engine.
     * @throws IOException thrown if engine could not open sockets.
     */
    @Throws(IOException::class)
    fun start()

    /**
     * Stop the engine.
     * @param timeout How long the engine will wait for pending processes to finish before a forced shutdown.
     * @param timeUnit The unit of time the engine will use to measure the timeout length.
     */
    @Throws(IOException::class)
    fun stop(
        timeout: Long = DEFAULT_TIMEOUT_SECONDS,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    )

    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
}