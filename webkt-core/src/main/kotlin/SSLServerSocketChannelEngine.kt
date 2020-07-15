import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine

abstract class SSLServerSocketChannelEngine(
    private val context: SSLContext,
    address: InetSocketAddress,
    service: ExecutorService,
    name: String = DEFAULT_THREAD_NAME
) : ServerSocketChannelEngine(address, service, name) {

    @Throws(IOException::class)
    override fun onAccept(channel: SocketChannel) {
        channel.configureBlocking(false)
        val engine: SSLEngine = context.createSSLEngine().apply {
            useClientMode = false
            needClientAuth = true
        }

        val sslChannel = SSLSocketChannel(channel, engine)
//        printSocketInfo(channel)

        /*
         * Start acceptance event call.
         */
        onAccept(sslChannel)

        /*
         * If channel is open after onAccept call,
         * register channel for read operations.
         */
        if (sslChannel.isOpen) {
            register(channel, sslChannel)
        }
    }

    @Throws(IOException::class)
    override fun onRead(channel: SocketChannel, attachment: Any?) {
        val sslChannel: SSLSocketChannel = attachment as SSLSocketChannel
        onRead(sslChannel)
        /*
         * If channel is open after onAccept call,
         * register channel for read operations.
         */
        if (!sslChannel.isOpen) {
            unregister(channel)
        }
    }

    @Throws(IOException::class)
    protected abstract fun onAccept(channel: SSLSocketChannel)

    @Throws(IOException::class)
    protected abstract fun onRead(channel: SSLSocketChannel)

    companion object {
        private const val DEFAULT_THREAD_NAME = "ssl-server-socket-channel-engine"

        private fun printSocketInfo(channel: SocketChannel, engine: SSLEngine) {
            println("Socket class: " + channel.javaClass)
            println("   Remote address = "
                + channel.remoteAddress.toString())
//            println("   Remote port = " + s.remoteAddress)
//            println("   Local socket address = "
//                + s.localAddress.toString())
            println("   Local address = "
                + channel.localAddress.toString())
//            println("   Local port = " + s.localPort)
            println("   Need client authentication = "
                + engine.needClientAuth)
            val ss = engine.session
            println("   Cipher suite = " + ss.cipherSuite)
            println("   Protocol = " + ss.protocol)
            println("   peerCerts = ${ss.peerCertificates}")
            println("   engine inbound done? = ${engine.isInboundDone}")
            println("   engine outbound done? = ${engine.isOutboundDone}")

        }
    }
}