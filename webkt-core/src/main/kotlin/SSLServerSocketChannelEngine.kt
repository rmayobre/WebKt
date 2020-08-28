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
    override fun onChannelAccepted(channel: SocketChannel) {
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
        onAcceptSSLChannel(sslChannel)

//        /*
//         * If channel is open after onAcceptSSLChannel call,
//         * register channel for read operations.
//         */
//        if (sslChannel.isOpen) {
//            registerToRead(channel, sslChannel)
//        }
    }

    @Throws(IOException::class)
    override fun onReadChannel(channel: SocketChannel, attachment: Any?) {
        val sslChannel: SSLSocketChannel = attachment as SSLSocketChannel
        onReadSSLChannel(sslChannel)
//        /*
//         * If channel is open after onAccept call,
//         * register channel for read operations.
//         */
//        if (!sslChannel.isOpen) {
//            unregister(channel)
//        }
    }

    override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    protected abstract fun onAcceptSSLChannel(channel: SSLSocketChannel)

    @Throws(IOException::class)
    protected abstract fun onReadSSLChannel(channel: SSLSocketChannel)

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