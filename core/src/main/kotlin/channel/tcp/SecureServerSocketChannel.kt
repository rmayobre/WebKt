package channel.tcp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.net.InetAddress
import java.net.ProtocolFamily
import java.net.SocketAddress
import java.nio.channels.Channel
import java.nio.channels.ServerSocketChannel
import javax.net.ssl.SSLContext
import kotlin.jvm.Throws

class SecureServerSocketChannel(
    channel: ServerSocketChannel,
    private val context: SSLContext,
    private val dispatcher: CoroutineDispatcher
) : SuspendedServerSocketChannel(channel) {

    var needClientAuth = false

    var wantClientAuth = false

    var enabledProtocols: Array<String> = emptyArray()

    var enabledCipherSuites: Array<String> = emptyArray()

    override fun accept(): SecureSocketChannel = SecureSocketChannel(
        channel = channel.accept()!!,
        engine = context.createSSLEngine().apply {
            useClientMode = false
            needClientAuth = this@SecureServerSocketChannel.needClientAuth
            wantClientAuth = this@SecureServerSocketChannel.wantClientAuth
            enabledProtocols = this@SecureServerSocketChannel.enabledProtocols
            enabledCipherSuites = this@SecureServerSocketChannel.enabledCipherSuites
        },
        dispatcher = dispatcher
    )

    override fun toString(): String =
        "SecureServerSocketChannel: ${hashCode()}\n" +
                "Channel Class:     ${channel.javaClass}\n" +
                "Local Address:     $localAddress\n" +
                "Local Port:        $localPort\n"

    companion object {
        /**
         * Opens a secure SecureServerSocketChannel. All SecureSocketChannels will have an
         * encrypted SSL connection.
         * @param context The SSLContext used to produce SSLEngines for each connection.
         * @param dispatcher The dispatcher that will handle the delegated tasks of the SSLEngines.
         * @param protocol A ProtocolFamily that can be applied to the open ServerSocketChannel
         * @throws IOException An I/O related error was thrown
         */
        @Throws(IOException::class)
        fun open(
            context: SSLContext = SSLContext.getDefault(),
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            protocol: ProtocolFamily? = null
        ): SecureServerSocketChannel = SecureServerSocketChannel(
            channel = protocol?.let {
                ServerSocketChannel.open(it)
            } ?: ServerSocketChannel.open(),
            context,
            dispatcher
        )
    }
}