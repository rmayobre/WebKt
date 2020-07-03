import java.io.IOException
import java.net.InetSocketAddress

/**
 *
 */
interface ServerEngine {

    /** Network address the server engine binds to. */
    val address: InetSocketAddress

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