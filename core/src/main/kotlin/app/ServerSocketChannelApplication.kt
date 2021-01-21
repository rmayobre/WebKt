package app

import channel.NetworkChannel
import channel.SuspendedSocketChannel
import engine.attachment.Attachment
import engine.attachment.SocketChannelAttachment
import engine.attachment.toTypeOf
import kotlinx.coroutines.*
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

abstract class ServerSocketChannelApplication<T : SuspendedSocketChannel>(
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
            key.isAcceptable ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(
                                channel = key.channel() as ServerSocketChannel,
                                attachment = key.attachment(),
                                error)
                        }
                    }
                ) {
                    onAccept(
                        channel = key.channel() as ServerSocketChannel,
                        attachment = key.attachment()
                    )
                }

            //
            // A channel, registered to the selector, has incoming data.
            //
            key.isReadable -> (key.attachment() as? Attachment<*>)?.toTypeOf<SuspendedSocketChannel> { channel, storage ->
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

                    if (channel is T) {
                        
                    }

                    onRead(
                        channel = channel as T,
                        attachment = key.attachment()
                    )
                }
            }

            //
            // A channel, registered to the selector, has data to be written.
            //
            key.isWritable -> (key.attachment() as? Attachment)?.let { attachment ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(
                                channel = key.channel(),
                                attachment = key.attachment(),
                                error
                            )
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
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that is able to
     * accept an incoming connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onAccept(channel: ServerSocketChannel, attachment: Any?)

    /**
     * An operations event of the engine.deprecated.AbstractChannelEngine. This means the Selector has provided a SelectionKey with a channel
     * that has incoming data being sent from the opposing endpoint.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onRead(channel: T, attachment: Any?)

    /**
     * An operations event of the engine.deprecated.AbstractChannelEngine. This means the Selector has provided a SelectionKey that is ready
     * for it's channel to write data.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected abstract suspend fun onWrite(channel: T, attachment: Any?)

    /**
     * Reports an exception occurred on a ServerSocketChannel.
     * @param error Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    protected abstract suspend fun onException(channel: ServerSocketChannel, attachment: Any?, error: Throwable)

    /**
     * Reports an exception occurred while processing a channel.
     * @param error Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    protected abstract suspend fun onException(channel: T, attachment: Any?, error: Throwable)
}