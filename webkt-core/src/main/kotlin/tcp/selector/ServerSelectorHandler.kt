package tcp.selector

import java.io.IOException
import java.nio.channels.SelectableChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeoutException

/**
 * A communication interface for a ServerSelectorRunnable. A ServerSelectorRunnable will communicate
 * with the ServerSelectorHandler based on events and operations required on selected Channels.
 * @see ServerSelectorRunnable
 */
interface ServerSelectorHandler {
    /**
     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
     * the channel to the appropriate settings during this function call.
     * @throws IOException when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    fun onChannelAccepted(channel: SocketChannel)

    /**
     * A channel is ready to be read by the engine's child implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    fun onReadChannel(channel: SocketChannel, attachment: Any?)

    /**
     * A channel is ready to be written by the engine's child implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    fun onWriteChannel(channel: SocketChannel, attachment: Any?)

    /**
     * Reports an exception occurred while processing a channel.
     * @param ex Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with SocketChannel
     */
    fun onException(channel: SelectableChannel, attachment: Any?, ex: Exception)
}