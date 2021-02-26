package channel.tcp

import channel.ServerChannel
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import javax.xml.ws.Dispatch
import kotlin.jvm.Throws

/**
 * A Non-blocking implementation of the ServerSocketChannel, designed to be used for
 * Kotlin and Kotlin's Coroutine library.
 */
open class SuspendedServerSocketChannel(
    final override val channel: ServerSocketChannel,
    /** This dispatcher will be  */
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

    init {
        channel.configureBlocking(false)
    }

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
         * @param address If an address is specified, the socket will bind immediately to the address.
         * @throws IOException An I/O related error was thrown
         */
        @Throws(IOException::class)
        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun open(
            address: SocketAddress? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): SuspendedServerSocketChannel =
            coroutineScope {
                SuspendedServerSocketChannel(
                    channel = ServerSocketChannel.open(),
                    dispatcher
                ).apply {
                    address?.let { bind(it) }
                }
            }
    }
}