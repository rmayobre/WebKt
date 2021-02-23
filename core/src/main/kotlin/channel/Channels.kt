package channel

import kotlinx.coroutines.CoroutineScope
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectableChannel
import kotlin.jvm.Throws

/**
 * A SuspendedChannel that connects to the machine's network.
 */
interface SuspendedNetworkChannel<T : SelectableChannel> : SuspendedChannel {

    /** The channel being wrapped. */
    val channel: T

    /** Get the socket's InetAddress */
    val inetAddress: InetAddress

    /** Get the channel's local address. */
    val localAddress: SocketAddress

    /** Get the channel's local port. */
    val localPort: Int

    /**
     * Binds the channel to the local address provided.
     * @param local an address the channel will take on the local network.
     * @throws IOException thrown if channel could not bind to address.
     */
    @Throws(IOException::class)
    suspend fun bind(local: SocketAddress)
}

/**
 * A SuspendedChannel that can read and write data.
 */
interface SuspendedByteChannel : SuspendedChannel {
    /**
     * A suspended process of reading from a socket connection into a buffer.
     * @param buffer the buffer where data is read into.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    @Throws(IOException::class)
    suspend fun read(buffer: ByteBuffer): Int

    /**
     * A suspended process of writing data into a socket connection.
     * @param buffer the data what will be written into the socket.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    @Throws(IOException::class)
    suspend fun write(buffer: ByteBuffer): Int
}

/**
 * A SuspendedChannel that orchestrates communication between endpoints. ClientChannels are able to send and receive
 * data and maintain the connection of a socket.
 */
interface ClientChannel<T : SelectableChannel> : SuspendedNetworkChannel<T>, SuspendedByteChannel {
    /** Get the channel's remote address. */
    val remoteAddress: SocketAddress

    /** Get the channel's remote port. */
    val remotePort: Int

    /**
     * Connect a client to the remote address. This will allow
     * a ClientNetworkChannel to send and receive data from the
     * remote address.
     * @param remote the targeted remote address.
     * @throws IOException Could not connect to remote address.
     */
    @Throws(IOException::class)
    suspend fun connect(remote: SocketAddress): Boolean
}

/**
 * A SuspendedChannel that accepts new incoming connections.
 */
interface ServerChannel<T : SelectableChannel, C : SelectableChannel> : SuspendedNetworkChannel<T> {
    /**
     * Accepts an incoming connection and constructs a SuspendedSocketChannel as
     * the connection's interface. Returns null if nothing to accept.
     * @throws IOException Issues while accepting new connection.
     */
    @Throws(IOException::class)
    fun accept(): ClientChannel<C>?
}

interface SuspendedChannel {
    /** Is the channel open? */
    val isOpen: Boolean

    /** The CoroutineScope that is operating the network jobs. */
    val scope: CoroutineScope

    /**
     * Close the channel and socket. If [wait] is set to TRUE, the channel will prevent any new suspend function calls
     * from creating jobs on the [SuspendedChannel.scope] CoroutineScope.
     * @param wait wait for jobs to finish? If this is marked FALSE, then it will cancel all jobs.
     */
    suspend fun close(wait: Boolean = false)
}