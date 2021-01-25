package engine

import app.NetworkApplication
import operation.OperationsChannel
import java.io.IOException
import kotlin.jvm.Throws

interface NetworkChannelEngine  {

    /**
     * Is the engine running?
     */
    val isRunning: Boolean

    /**
     * Start your engine.
     */
    fun NetworkApplication.start(/* Create some kind of interface to communicate with. */): OperationsChannel


    /**
     * Stop the engine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop()
}

