import java.io.IOException
import java.util.concurrent.TimeUnit

interface ServerEngine {
    /**
     * Start your engine.
     * @param blocking configure the Server's socket to blocking or non-blocking.
     * @throws IOException thrown if engine could not open sockets.
     */
    @Throws(IOException::class)
    fun start(blocking: Boolean)

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