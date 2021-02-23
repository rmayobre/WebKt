package engine

import app.NetworkApplication
import channel.SuspendedNetworkChannel
import operation.OperationsChannel
import java.io.IOException
import kotlin.jvm.Throws

interface NetworkChannelEngine<T : SuspendedNetworkChannel<*>>  {

    /**
     * Is the engine running?
     */
    val isRunning: Boolean

    /**
     * Start your engine.
     */
    fun NetworkApplication.start(/* Create some kind of interface to communicate with. */): OperationsChannel<T>


    /**
     * Stop the engine.
     * @param timeout How long to wait, in milliseconds, for the NetworkChannelEngine to stop.
     * @param cause A throwable to send to all running operations within the NetworkChannelEngine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop(
        timeout: Long? = null,
        cause: Throwable? = null
    )
}

