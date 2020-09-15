package tcp.ssl

import tcp.AbstractServerSocketChannelEngine
import tcp.selector.READ_OPERATION
import tcp.selector.ServerSelectorHandler
import tcp.selector.WRITE_OPERATION
import tcp.ssl.factory.SSLSocketChannelFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import javax.net.ssl.SSLContext

/**
 * Secure Socket Layer implementation for an AbstractServerSocketChannelEngine.
 */
abstract class SSLServerSocketChannelEngine(
    address: InetSocketAddress,
    service: ExecutorService,
    threadName: String = DEFAULT_THREAD_NAME
): AbstractServerSocketChannelEngine(address, service, threadName) {

    protected abstract val factory: SSLSocketChannelFactory

    override val handler: ServerSelectorHandler = object : ServerSelectorHandler {
        override fun onChannelAccepted(channel: SocketChannel) {
            val sslChannel = factory.create(channel)
            onSSLHandshake(sslChannel)
        }

        override fun onReadChannel(channel: SocketChannel, attachment: Any?) {
            val sslChannel: SSLSocketChannel = attachment as SSLSocketChannel
            onReadSSLChannel(sslChannel)
        }

        override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
            val bundle: SSLWriteBundle = attachment as SSLWriteBundle
            onWriteSSLChannel(bundle.channel, bundle.data)
        }

        override fun onException(channel: SelectableChannel, attachment: Any?, ex: Exception) {
            when (attachment) {
                is SSLSocketChannel ->  onException(attachment, null, ex)
                is SSLWriteBundle -> onException(attachment.channel, attachment.data, ex)
                else -> onException(channel, attachment, ex)
            }
        }
    }

    /**
     * Register channel into Selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see register
     */
    @Throws(IOException::class)
    protected fun registerToRead(channel: SSLSocketChannel) =
        register(channel.channel, READ_OPERATION, channel)

    /**
     * Register channel into Selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see register
     */
    @Throws(IOException::class)
    protected fun registerToWrite(channel: SSLSocketChannel, data: Any? = null) =
        register(channel.channel, WRITE_OPERATION, SSLWriteBundle(channel, data))

    /**
     * A new SSLSocketChannel was created and is ready to perform an SSL handshake.
     * @throws IOException thrown from IO operations with SSLSocketChannel; can occur while performing handshake.
     */
    @Throws(IOException::class)
    protected abstract fun onSSLHandshake(channel: SSLSocketChannel)

    /**
     * A channel is ready to be read from.
     * @throws IOException thrown from IO operations with SSLSocketChannel
     */
    @Throws(IOException::class)
    protected abstract fun onReadSSLChannel(channel: SSLSocketChannel)

    /**
     * A channel is ready to be written to.
     * @throws IOException thrown from IO operations with SSLSocketChannel
     */
    @Throws(IOException::class)
    protected abstract fun onWriteSSLChannel(channel: SSLSocketChannel, data: Any?)

    /**
     * An exception occurred while using the provided SSLSocketChannel.
     * @param channel SSLSocketChannel relating to the thrown exception.
     * @param attachment Any attachments provided with Channel.
     * @param ex The exception thrown.
     */
    protected abstract fun onException(channel: SSLSocketChannel, attachment: Any?, ex: Exception)

    /**
     * An exception occurred while using the provided Channel. The channel was not a SSLSocketChannel.
     * This could mean that the SelectableChannel is the ServerSocketChannel, or a SocketChannel that
     * had issues being made into a SSLSocketChannel.
     * @param channel Channel related to exception thrown.
     * @param attachment Any attachments provided with Channel.
     * @param ex The exception thrown.
     */
    protected abstract fun onException(channel: SelectableChannel, attachment: Any?, ex: Exception)

    /**
     * A bundle created during a [registerToWrite] call. It is passed to the Selector as an
     * attachment and carries the SSLSocketChannel, as well as the data to be written in the
     * [onWriteSSLChannel] event.
     */
    protected class SSLWriteBundle(
        /** Channel to write to. */
        val channel: SSLSocketChannel,
        /** An object that can be used to help with write operations. */
        val data: Any?
    )

    companion object {
        private const val DEFAULT_THREAD_NAME = "ssl-server-socket-channel-engine"
    }
}