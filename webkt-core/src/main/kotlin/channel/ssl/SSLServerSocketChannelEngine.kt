package channel.ssl

import channel.ServerSocketChannelEngine
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import javax.net.ssl.SSLContext

abstract class SSLServerSocketChannelEngine(
    private val context: SSLContext,
    address: InetSocketAddress,
    service: ExecutorService,
    name: String = DEFAULT_THREAD_NAME
) : ServerSocketChannelEngine(address, service, name) {

    @Throws(IOException::class)
    override fun onChannelAccepted(channel: SocketChannel) {
        val engine = context.createSSLEngine().apply {
            useClientMode = false
            needClientAuth = true
        }
        val sslChannel = SSLSocketChannel(channel, engine)
        onSSLHandshake(sslChannel)
    }

    @Throws(IOException::class)
    override fun onReadChannel(channel: SocketChannel, attachment: Any?) {
        val sslChannel: SSLSocketChannel = attachment as SSLSocketChannel
        onReadSSLChannel(sslChannel)
    }

    override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
        val bundle: SSLWriteBundle = attachment as SSLWriteBundle
        onWriteSSLChannel(bundle.channel, bundle.data)
    }

    @Throws(IOException::class)
    protected fun registerToRead(channel: SSLSocketChannel) =
        registerToRead(channel.channel, channel)

    @Throws(IOException::class)
    protected fun registerToWrite(channel: SSLSocketChannel, data: Any? = null) =
            registerToWrite(channel.channel, SSLWriteBundle(channel, data))

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
     * A bundle created during a [registerToWrite] call. It is passed to the Selector as an
     * attachment and carries the SSLSocketChannel, as well as the data to be written in the
     * [onWriteSSLChannel] event.
     */
    protected class SSLWriteBundle(
        /** Channel to write to. */
        val channel: SSLSocketChannel,
        /** Data to be written. */
        val data: Any?
    )

    companion object {
        private const val DEFAULT_THREAD_NAME = "channel.ssl-server-socket-channel-engine"
    }
}