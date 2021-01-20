package engine

import engine.operation.OperationMessage
import kotlinx.coroutines.channels.SendChannel
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
    fun start(vararg message: OperationMessage): SendChannel<OperationMessage>


    /**
     * Stop the engine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop()
}

