package channel.tcp

import channel.ClientNetworkChannel
import channel.SuspendedByteChannel
import java.io.IOException
import java.nio.ByteBuffer
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import kotlin.jvm.Throws

open class SuspendedSocketChannel(
    final override val channel: SocketChannel
) : SuspendedByteChannel, ClientNetworkChannel<SocketChannel>, Channel by channel {

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


    /**
     * Constructs a SuspendedSocketChannel with a standard SocketChannel. The SocketChannel is not
     * connected to a remote address yet.
     * @throws IOException An I/O related error was thrown
     */
    @Throws(IOException::class)
    constructor(): this(SocketChannel.open())

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
    constructor(remote: SocketAddress): this(SocketChannel.open(remote))

    init {
        if (channel.isBlocking) {
            channel.configureBlocking(false)
        }
    }

    override fun bind(local: SocketAddress) {
        channel.bind(local)
    }

    override fun connect(remote: SocketAddress) {
        channel.connect(remote)
    }

    override suspend fun read(buffer: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override suspend fun write(buffer: ByteBuffer): Int {

        TODO("Not yet implemented")
    }

    override fun toString(): String =
        "SuspendedSocketChannel: ${hashCode()}\n" +
            "Channel Class:     ${channel.javaClass}\n" +
            "Remote Address:    $remoteAddress\n" +
            "Remote Port:       $remotePort\n" +
            "Local Address:     $localAddress\n" +
            "Local Port:        $localPort\n"
}