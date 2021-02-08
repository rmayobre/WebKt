package engine

import app.NetworkApplication
import channel.NetworkChannel
import operation.OperationsChannel
import java.io.IOException
import kotlin.jvm.Throws

interface NetworkChannelEngine<T : NetworkChannel<*>>  {

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
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop()
}

