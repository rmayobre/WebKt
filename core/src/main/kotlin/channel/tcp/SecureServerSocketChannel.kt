package channel.tcp

import kotlinx.coroutines.*
import java.io.IOException
import java.net.SocketAddress
import java.nio.channels.ServerSocketChannel
import javax.net.ssl.SSLContext
import kotlin.jvm.Throws

class SecureServerSocketChannel(
    channel: ServerSocketChannel,
    private val context: SSLContext = SSLContext.getDefault(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendedServerSocketChannel(channel, dispatcher) {

    var needClientAuth = false

    var wantClientAuth = false

    var enabledProtocols: Array<String> = emptyArray()

    var enabledCipherSuites: Array<String> = emptyArray()

    @ObsoleteCoroutinesApi
    override fun accept(): SecureSocketChannel? =
        channel.accept()?.let {
            SecureSocketChannel(
                channel = it,
                engine = context.createSSLEngine().apply {
                    useClientMode = false
                    needClientAuth = this@SecureServerSocketChannel.needClientAuth
                    wantClientAuth = this@SecureServerSocketChannel.wantClientAuth
                    enabledProtocols = this@SecureServerSocketChannel.enabledProtocols
                    enabledCipherSuites = this@SecureServerSocketChannel.enabledCipherSuites
                },
                dispatcher
            )
        }

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
         * @throws IOException An I/O related error was thrown
         */
        @Throws(IOException::class)
        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun open(
            address: SocketAddress? = null,
            context: SSLContext = SSLContext.getDefault(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): SecureServerSocketChannel =
            coroutineScope {
                SecureServerSocketChannel(
                    channel = ServerSocketChannel.open().apply {
                        configureBlocking(false)
                        address?.let { bind(it) }
                    },
                    context,
                    dispatcher
                )
            }
    }
}