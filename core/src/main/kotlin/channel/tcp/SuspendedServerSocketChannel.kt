package channel.tcp

import channel.ServerChannel
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import kotlin.jvm.Throws

/**
 * A Non-blocking implementation of the ServerSocketChannel, designed to be used for
 * Kotlin and Kotlin's Coroutine library.
 */
open class SuspendedServerSocketChannel(
    override val channel: ServerSocketChannel,
    protected val dispatcher: CoroutineDispatcher = Dispatchers.IO
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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun bind(local: SocketAddress): Unit =
        withContext(scope.coroutineContext) {
            channel.bind(local)
        }

    override fun accept(): SuspendedSocketChannel? =
        channel.accept()?.let {
            SuspendedSocketChannel(it, dispatcher)
        }

    @Suppress("BlockingMethodInNonBlockingContext")
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
        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun open(address: SocketAddress? = null): SuspendedServerSocketChannel =
            coroutineScope {
                SuspendedServerSocketChannel(
                    channel = ServerSocketChannel.open().apply {
                        configureBlocking(false)
                        address?.let { bind(it) }
                    }
                )
            }
    }
}