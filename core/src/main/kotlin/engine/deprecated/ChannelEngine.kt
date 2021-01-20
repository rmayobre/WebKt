package engine.deprecated

import java.io.IOException
import java.nio.channels.SelectableChannel
import kotlin.jvm.Throws

/**
 * An engine that hosts network operations.
 */
@Deprecated("Use NetworkChannel Engine")
interface ChannelEngine {

    /**
     * Is the engine running?
     */
    val isRunning: Boolean

    /**
     * Start your engine.
     * @throws IOException thrown if engine could not open sockets.
     */
    @Throws(IOException::class)
    fun start()//: Boolean TODO this should return a sendChannel.


    /**
     * Stop the engine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    fun stop()//: Boolean

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @param channel SelectableChannel to be registered to Selector.
     * @param operation Operation the Channel will be registered to perform. NOTE, you can register multiple operations at the same time.
     * @param attachment An attachment to be provided for the channel's next engine.operation.
     */
    fun register(channel: SelectableChannel, operation: Operation, attachment: Any? = null)
}