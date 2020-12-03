import java.io.IOException

/**
 * An engine that hosts network operations.
 */
interface ServerEngine {

    /**
     * Is the ServerEngine running? Returns true if so.
     */
    val isRunning: Boolean

    /**
     * Start your engine.
     * @throws IOException thrown if engine could not open sockets.
     */
    @Throws(IOException::class)
    fun start()

    /**
     * Stop the engine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop()
}