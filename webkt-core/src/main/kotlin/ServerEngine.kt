import java.io.IOException
import java.net.InetSocketAddress

/**
 *
 */
interface ServerEngine {

    /**
     * Determine if Engine is running
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
     */
    @Throws(IOException::class)
    fun stop()
}