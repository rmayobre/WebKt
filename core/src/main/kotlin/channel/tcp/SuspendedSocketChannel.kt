package channel.tcp

import channel.ClientChannel
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import kotlin.jvm.Throws

open class SuspendedSocketChannel(
    override val channel: SocketChannel,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ClientChannel<SocketChannel> {

    private val job = Job()

    private var isClosing: Boolean = false

    override val isOpen: Boolean
        get() = channel.isOpen && !isClosing

    override val scope: CoroutineScope =
        CoroutineScope(dispatcher + job)

    override val inetAddress: InetAddress
        get() = channel.socket().inetAddress

    override val remoteAddress: SocketAddress
        get() = channel.remoteAddress

    override val remotePort: Int
        get() = channel.socket().port

    override val localAddress: SocketAddress
        get() = channel.localAddress

    override val localPort: Int
        get() = channel.socket().localPort

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun bind(local: SocketAddress): Unit =
        withContext(scope.coroutineContext) {
            channel.bind(local)
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun connect(remote: SocketAddress): Boolean =
        withContext(scope.coroutineContext) {
            channel.connect(remote)
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun read(buffer: ByteBuffer): Int =
        if (!isClosing) { 
            var read = 0
            do {
                val prev: Int = withContext(scope.coroutineContext) {
                    channel.read(buffer)
                }
                read += if (prev > -1) prev else 0
            } while(buffer.hasRemaining() && prev > 0)
            read
        } else 0

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun write(buffer: ByteBuffer): Int =
        if (!isClosing) {
            var written = 0
            do {
                val prev: Int = withContext(scope.coroutineContext) {
                    channel.write(buffer)
                }
                written += if (prev > -1) prev else 0
            } while (buffer.hasRemaining() && prev  > 0)
            written
        } else 0


    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun close(wait: Boolean) {
        if (!isClosing) {
            isClosing = true
            if (wait) {
                job.join()
            } else {
                job.cancel()
            }
            channel.close()
            isClosing = false // channel is closed.
        }
    }

    override fun toString(): String =
        "SuspendedSocketChannel: ${hashCode()}\n" +
            "Channel Class:     ${channel.javaClass}\n" +
            "Remote Address:    $remoteAddress\n" +
            "Remote Port:       $remotePort\n" +
            "Local Address:     $localAddress\n" +
            "Local Port:        $localPort\n"

    companion object {

        /**
         * Open a SuspendedSocketChannel with the provided SocketAddress. If no SocketAddress is provided,
         * then the Channel will not be connected.
         * @throws AsynchronousCloseException
         *         If another thread closes this channel
         *         while the connect operation is in progress
         * @throws ClosedByInterruptException
         *         If another thread interrupts the current thread
         *         while the connect operation is in progress, thereby
         *         closing the channel and setting the current thread's
         *         interrupt status
         * @throws UnresolvedAddressException
         *         If the given remote address is not fully resolved
         * @throws UnsupportedAddressTypeException
         *         If the type of the given remote address is not supported
         * @throws SecurityException
         *         If a security manager has been installed
         *         and it does not permit access to the given remote endpoint
         * @throws IOException Could not open a network connection.
         */
        @Throws(IOException::class)
        @Suppress("BlockingMethodInNonBlockingContext")
        suspend fun open(remote: SocketAddress? = null): SuspendedSocketChannel =
            coroutineScope {
                withContext(coroutineContext) {
                    SuspendedSocketChannel(
                        channel = (remote?.let {
                            SocketChannel.open(it)
                        } ?: SocketChannel.open()).apply {
                            configureBlocking(false)
                        }
                    )
                }
            }
    }
}