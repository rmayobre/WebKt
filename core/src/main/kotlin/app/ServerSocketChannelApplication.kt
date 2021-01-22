package app

import channel.tcp.SuspendedSocketChannel
import engine.Attachment
import engine.operation.OperationsChannel
import engine.toTypeOf
import kotlinx.coroutines.*
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

abstract class ServerSocketChannelApplication(
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
            key.isReadable -> (key.attachment() as? Attachment<*>)?.toTypeOf<SuspendedSocketChannel> { channel, attachment ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(channel, attachment, error)
                        }
                    }
                ) {
                    onRead(channel, attachment)
                }
            }

            //
            // A channel, registered to the selector, has data to be written.
            //
            key.isWritable -> (key.attachment() as? Attachment<*>)?.toTypeOf<SuspendedSocketChannel> { channel, attachment ->
                launch(
                    CoroutineExceptionHandler { _, error ->
                        launch {
                            onException(channel, attachment, error)
                        }
                    }
                ) {
                    onWrite(channel, attachment)
                }
            }
        }
    }

    /**
     * An operations event of the ServerSocketChannel. The Channel is ready to accept new connections.
     * @param channel the channel that will accept new connections.
     * @param attachment an object associated with the channel. To register an attachment to a channel
     *                   you must attach it through the OperationChannel.
     * @see OperationsChannel
     */
    protected abstract suspend fun onAccept(channel: ServerSocketChannel, attachment: Any?)

    /**
     * An operations event of the SuspendedSocketChannel. The Channel is ready to be written.
     * @param channel the channel to be written to.
     * @param attachment an object associated with the channel. To register an attachment to a channel
     *                   you must attach it through the OperationChannel.
     * @see OperationsChannel
     */
    protected abstract suspend fun onRead(channel: SuspendedSocketChannel, attachment: Any?)

    /**
     * An operations event of the SuspendedSocketChannel. The Channel is ready to be written.
     * @param channel the channel to be written to.
     * @param attachment an object associated with the channel. To register an attachment to a channel
     *                   you must attach it through the OperationChannel.
     * @see OperationsChannel
     */
    protected abstract suspend fun onWrite(channel: SuspendedSocketChannel, attachment: Any?)

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
    protected abstract suspend fun onException(channel: SuspendedSocketChannel, attachment: Any?, error: Throwable)
}