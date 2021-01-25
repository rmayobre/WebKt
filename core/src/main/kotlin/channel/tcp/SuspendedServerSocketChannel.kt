package channel.tcp

import channel.ServerChannel
import java.io.IOException
import java.net.InetAddress
import java.net.ProtocolFamily
import java.net.SocketAddress
import java.nio.channels.*
import kotlin.jvm.Throws

/**
 * A Non-blocking implementation of the ServerSocketChannel, designed to be used for
 * Kotlin and Kotlin's Coroutine library.
 */
open class SuspendedServerSocketChannel(
    override val channel: ServerSocketChannel,
) : ServerChannel<ServerSocketChannel, SocketChannel>, Channel by channel {

    override val inetAddress: InetAddress
        get() = channel.socket().inetAddress

    override val localAddress: SocketAddress
        get() = channel.localAddress

    override val localPort: Int
        get() = channel.socket().localPort

    override fun bind(local: SocketAddress) {
        channel.bind(local)
    }

    override fun accept(): SuspendedSocketChannel =
        SuspendedSocketChannel(channel.accept()!!)

    override fun toString(): String =
        "SuspendedServerSocketChannel: ${hashCode()}\n" +
                "Channel Class:     ${channel.javaClass}\n" +
                "Local Address:     $localAddress\n" +
                "Local Port:        $localPort\n"

    companion object {
        /**
         * Opens a standard ServerSocketChannel and constructs a SuspendedServerSocketChannel.
         * @param protocol Set a ProtocolFamily to the SeverSocketChannel.
         * @throws IOException An I/O related error was thrown
         */
        @Throws(IOException::class)
        fun open(protocol: ProtocolFamily? = null): SuspendedServerSocketChannel =
            SuspendedServerSocketChannel(
                channel = (protocol?.let {
                    ServerSocketChannel.open(it)
                } ?: ServerSocketChannel.open()).apply {
                    configureBlocking(false)
                }
            )
    }
}