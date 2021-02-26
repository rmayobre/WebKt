package app

import MissingAttachmentException
import channel.tcp.SuspendedServerSocketChannel
import channel.tcp.SuspendedSocketChannel
import engine.Attachment
import engine.NetworkChannelEngine
import operation.OperationsChannel
import kotlinx.coroutines.*
import java.nio.channels.SelectionKey

/**
 * A Server-side [NetworkApplication] that will handle networking operations. This [NetworkApplication]
 * is designed to run on a [SuspendedServerSocketChannel] and accept incoming connections as a
 * [SuspendedSocketChannel]. The SuspendedServerApplication will handle the logice for accepting
 * [SuspendedSocketChannel]s and read/write operations for the accepted [SuspendedServerSocketChannel]s.
 */
abstract class SuspendedServerApplication(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): NetworkApplication {

    final override val appScope: CoroutineScope =
            CoroutineScope(SupervisorJob() + dispatcher)

    final override fun onValidKey(key: SelectionKey) {
        try {
            // What operation is the key ready for?
            when {
                // A channel has able to connect to remote address.
                key.isAcceptable -> (key.attachment() as? Attachment<*>)?.onAcceptable()
                    ?: throw MissingAttachmentException()
                // A channel, registered to the selector, has incoming data.
                key.isReadable -> (key.attachment() as? Attachment<*>)?.onReadable()
                    ?: throw MissingAttachmentException()
                // A channel, registered to the selector, has data to be written.
                key.isWritable -> (key.attachment() as? Attachment<*>)?.onWritable()
                    ?: throw MissingAttachmentException()
            }
        } catch (ex: Exception) {
            appScope.launch {
                onException(key, ex)
            }
        }
    }

    private fun Attachment<*>.onAcceptable(): Unit = with(appScope) {
        if (channel is SuspendedServerSocketChannel) {
            launch(context = CoroutineExceptionHandler { _, error ->
                launch {
                    onException(channel, storage, error)
                }
            }) {
                onAccept(channel, storage)
            }
        }
    }

    private fun Attachment<*>.onReadable(): Unit = with(appScope) {
        if (channel is SuspendedSocketChannel) {
            launch(context = CoroutineExceptionHandler { _, error ->
                launch {
                    onException(channel, storage, error)
                }
            }
            ) {
                onRead(channel, storage)
            }
        }
    }

    private fun Attachment<*>.onWritable(): Unit = with(appScope) {
        if (channel is SuspendedSocketChannel) {
            launch(context = CoroutineExceptionHandler { _, error ->
                launch {
                    onException(channel, storage, error)
                }
            }) {
                onWrite(channel, storage)
            }
        }
    }


    /** The NetworkChannelEngine required to run the application. */
    protected abstract val engine: NetworkChannelEngine

    /**
     * An operations event of the ServerSocketChannel. The Channel is ready to accept new connections.
     * @param channel the channel that will accept new connections.
     * @param attachment an object associated with the channel. To register an attachment to a channel
     *                   you must attach it through the OperationChannel.
     * @see OperationsChannel
     */
    protected abstract suspend fun onAccept(channel: SuspendedServerSocketChannel, attachment: Any?)

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
     * A SelectionKey was missing it's attachments or an unhandled exception occurred.
     * @param key The SelectionKey within context of the exception.
     * @param cause The exception thrown.
     */
    protected abstract suspend fun onException(key: SelectionKey, cause: Throwable)

    /**
     * Reports an exception occurred on a ServerSocketChannel.
     * @param cause Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    protected abstract suspend fun onException(channel: SuspendedServerSocketChannel, attachment: Any?, cause: Throwable)

    /**
     * Reports an exception occurred while processing a channel.
     * @param cause Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with the SelectableChannel
     */
    protected abstract suspend fun onException(channel: SuspendedSocketChannel, attachment: Any?, cause: Throwable)
}