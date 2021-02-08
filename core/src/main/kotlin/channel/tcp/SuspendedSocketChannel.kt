package channel.tcp

import channel.ClientChannel
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.ByteBuffer
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import kotlin.jvm.Throws

open class SuspendedSocketChannel(
    override val channel: SocketChannel,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ClientChannel<SocketChannel> {

    private val job = Job()

    override val isOpen: Boolean
        get() = channel.isOpen

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

    override fun bind(local: SocketAddress) {
        // TODO handle exceptions
        channel.bind(local)
    }

    override fun connect(remote: SocketAddress) {
        // TODO handle exceptions
        channel.connect(remote)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun read(buffer: ByteBuffer): Int  {
        var read = 0
        var prev = 0
        scope.launch {
            do {
                prev = channel.read(buffer)
                if (prev != -1) {
                    read += prev
                }
            } while(buffer.hasRemaining() && prev != -1)
        }.join() // wait for this job to finish.
        return read
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun write(buffer: ByteBuffer): Int {
        var written = 0
        var prev = 0
        scope.launch {
            while(buffer.hasRemaining() && prev != -1) {
                prev = channel.write(buffer)
                written += prev
            }
        }.join() // wait for this job to finish.
        return written
    }

    override suspend fun close(wait: Boolean) {
        //TODO implement waiting
        job.cancel()
        channel.close()
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
         * Constructs a SuspendedSocketChannel with a standard SocketChannel. The SocketChannel is not
         * connected to a remote address yet.
         * @throws IOException An I/O related error was thrown
         */
        @Throws(IOException::class)
        fun open(): SuspendedSocketChannel =
            open(SocketChannel.open())

        /**
         * Constructs a SuspendedSocketChannel and connect to a remote address.
         *
         * @throws AsynchronousCloseException
         *         If another thread closes this channel
         *         while the connect operation is in progress
         *
         * @throws ClosedByInterruptException
         *         If another thread interrupts the current thread
         *         while the connect operation is in progress, thereby
         *         closing the channel and setting the current thread's
         *         interrupt status
         *
         * @throws UnresolvedAddressException
         *         If the given remote address is not fully resolved
         *
         * @throws UnsupportedAddressTypeException
         *         If the type of the given remote address is not supported
         *
         * @throws SecurityException
         *         If a security manager has been installed
         *         and it does not permit access to the given remote endpoint
         *
         * @throws IOException An I/O related error was thrown
         */
        @Throws(
            AsynchronousCloseException::class,
            ClosedByInterruptException::class,
            UnresolvedAddressException::class,
            UnsupportedAddressTypeException::class,
            SecurityException::class,
            IOException::class
        )
        fun open(remote: SocketAddress): SuspendedSocketChannel =
            open(SocketChannel.open(remote))

        fun open(channel: SocketChannel): SuspendedSocketChannel =
            SuspendedSocketChannel(
                channel = channel.apply {
                    configureBlocking(false)
//                connect() TODO make this an async connection - read the connect method.
                }
            )
    }
}