package channel.tcp

import channel.ServerChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ServerChannel<ServerSocketChannel, SocketChannel> {

    private val job = Job()

    private var closing: Boolean = false

    override val scope: CoroutineScope =
        CoroutineScope(dispatcher + job)

    override val isOpen: Boolean
        get() = channel.isOpen && !closing

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

    override suspend fun close(wait: Boolean) {
        if (!closing) {
            closing = true
            if (wait) {
                job.join()
            }
            channel.close()
            closing = false // channel is closed.
        }
    }

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
        fun open(address: SocketAddress? = null): SuspendedServerSocketChannel =
            SuspendedServerSocketChannel(
                channel = ServerSocketChannel.open().apply {
                    configureBlocking(false)
                    address?.let { bind(it) }
                }
            )
    }
}