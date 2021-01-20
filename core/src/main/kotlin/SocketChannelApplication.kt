import kotlinx.coroutines.*
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

abstract class SocketChannelApplication(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): NetworkApplication {

    private val supervisor = SupervisorJob()

    override val appScope: CoroutineScope =
        CoroutineScope(supervisor + dispatcher)

    override fun onValidKey(key: SelectionKey): Unit = with(appScope) {
        // Remove key from selector to prevent
        // the key from being processed again.
        key.cancel()

        // What operation is the key ready for?
        when {

            //
            // A channel has able to connect to remote address.
            //

            key.isConnectable ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(
                                channel = key.channel(),
                                attachment = key.attachment(),
                                error)
                        }
                    }
                ) {
                    onConnect(
                        channel = key.channel() as SocketChannel,
                        attachment = key.attachment()
                    )
                }

            //
            // A channel, registered to the selector, has incoming data.
            //

            key.isReadable ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(
                                channel = key.channel(),
                                attachment = key.attachment(),
                                error)
                        }
                    }
                ) {
                    onRead(
                        channel = key.channel() as SocketChannel,
                        attachment = key.attachment()
                    )
                }

            //
            // A channel, registered to the selector, has data to be written.
            //

            key.isWritable ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(
                                channel = key.channel(),
                                attachment = key.attachment(),
                                error)
                        }
                    }
                ) {
                    onWrite(
                        channel = key.channel() as SocketChannel,
                        attachment = key.attachment()
                    )
                }
        }
    }

    /**
     * An operations event of the engine.deprecated.AbstractChannelEngine. This means the Selector has provided a SelectionKey that has
     * a channel, ready to finish connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onConnect(channel: SocketChannel, attachment: Any?)

    /**
     * An operations event of the engine.deprecated.AbstractChannelEngine. This means the Selector has provided a SelectionKey with a channel
     * that has incoming data being sent from the opposing endpoint.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onRead(channel: SocketChannel, attachment: Any?)

    /**
     * An operations event of the engine.deprecated.AbstractChannelEngine. This means the Selector has provided a SelectionKey that is ready
     * for it's channel to write data.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onWrite(channel: SocketChannel, attachment: Any?)

    /**
     * Reports an exception occurred while processing a channel.
     * @param error Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    protected abstract suspend fun onException(channel: SelectableChannel, attachment: Any?, error: Throwable)
}