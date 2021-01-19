package engine.operation.handler

import java.nio.channels.SelectableChannel
import kotlin.jvm.Throws

/**
 * A communication interface for a ServerSelectorRunnable. A ServerSelectorRunnable will communicate
 * with the ServerSelectorHandler based on operations available on selected Channels.
 * @see ServerChannelEngine
 */
interface ServerOperationsHandler { // TODO make this an actor
    /**
     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
     * the channel to the appropriate settings during this function call.
     * @throws Exception when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(Exception::class)
    suspend fun onChannelAccepted(channel: SelectableChannel, attachment: Any?)

    /**
     * A channel is ready to be read by the engine's child implementation.
     * @throws Exception when a channel cannot be read from.
     */
    @Throws(Exception::class)
    suspend fun onReadChannel(channel: SelectableChannel, attachment: Any?)

    /**
     * A channel is ready to be written by the engine's child implementation.
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