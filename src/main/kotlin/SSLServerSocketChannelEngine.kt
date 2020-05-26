import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine

abstract class SSLServerSocketChannelEngine(
    private val context: SSLContext,
    address: InetSocketAddress,
    service: ExecutorService,
    name: String? = null
) : ServerSocketChannelEngine(address, service, name ?: DEFAULT_THREAD_NAME) {

    override fun onRead(channel: SocketChannel) {
        val engine: SSLEngine = context.createSSLEngine().apply {
            useClientMode = false
            needClientAuth = true
            beginHandshake()
        }
        val sslChannel = SSLSocketChannel(channel, engine)
        if (sslChannel.performHandshake()) {
            onRead(sslChannel)
        } else {
            sslChannel.close()
        }
    }

    protected abstract fun onRead(channel: SSLSocketChannel)

    companion object {
        private const val DEFAULT_THREAD_NAME = "ssl-server-socket-channel-thread"
    }
}