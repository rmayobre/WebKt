import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
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
    override fun onAccept(key: SelectionKey) {
        (key.channel() as ServerSocketChannel).accept()?.let { channel ->
            channel.configureBlocking(false)
            val engine: SSLEngine = context.createSSLEngine().apply {
                useClientMode = false
                beginHandshake()
            }

            val sslChannel = SSLSocketChannel(channel, engine)

            // TODO If handshake is complete, register channel; otherwise close channel.

            if (sslChannel.performHandshake()) {
                register(channel, sslChannel)
            } else {
                sslChannel.close()
            }
        }
    }

    @Throws(IOException::class)
    override fun onRead(key: SelectionKey) {
        val channel: SSLSocketChannel = key.attachment() as SSLSocketChannel
        TODO("Read from SSLSocketChannel")
    }
/*
    override fun onRead(channel: SocketChannel) {
        val engine: SSLEngine = context.createSSLEngine().apply {
            useClientMode = false
//            needClientAuth = true
            beginHandshake()
        }
        val sslChannel = SSLSocketChannel(channel, engine)
        try {
            sslChannel.handshake()
            onRead(sslChannel)
        } catch (ex: IOException) {
            sslChannel.close()
        }
        printSocketInfo(channel, engine)
//        if (sslChannel.performHandshake()) {
//            onRead(sslChannel)
//        } else {
//            sslChannel.close()
//        }
    }

 */

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