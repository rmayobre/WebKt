package engine.operation.handler

import java.nio.channels.SelectableChannel
import kotlin.jvm.Throws

/**
 * A communication interface for a ClientSelectorRunnable. A ClientSelectorRunnable will communicate
 * with the ClientChannelOperationHandler based on the operations available on selected Channels.
 * @see ClientChannelEngine
 */
interface ClientOperationsHandler { // TODO make this an actor
    /**
     * Client Channel is ready to finish connection.
     * @throws Exception when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(Exception::class)
    suspend fun onConnect(channel: SelectableChannel)

    /**
     * Client channel is ready to be read.
     * @throws Exception when a channel cannot be read from.
     */
    @Throws(Exception::class)
    suspend fun onReadChannel(channel: SelectableChannel, attachment: Any?)

    /**
     * Client channel is ready to be written.
     * @throws Exception when a channel cannot be read from.
     */
    @Throws(Exception::class)
    suspend fun onWriteChannel(channel: SelectableChannel, attachment: Any?)

    /**
     * Reports an exception occurred while processing a channel.
     * @param error Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    suspend fun onException(channel: SelectableChannel, attachment: Any?, error: Throwable)
}