package channel

import kotlinx.coroutines.CoroutineScope
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectableChannel

interface NetworkChannel<T : SelectableChannel> : SuspendedChannel {

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
     */
    fun bind(local: SocketAddress)
}

interface SuspendedByteChannel : SuspendedChannel {
    /**
     * A suspended process of reading from a socket connection into a buffer.
     * @param buffer the buffer where data is read into.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    suspend fun read(buffer: ByteBuffer): Int

    /**
     * A suspended process of writing data into a socket connection.
     * @param buffer the data what will be written into the socket.
     * @throws IOException thrown if there is a problem reading from socket.
     */
    suspend fun write(buffer: ByteBuffer): Int
}

/**
 * A SuspendedChannel that orchestrates communication between endpoints. ClientChannels are able to send and receive
 * data and maintain the connection of a socket.
 */
interface ClientChannel<T : SelectableChannel> : NetworkChannel<T>, SuspendedByteChannel {
    /** Get the channel's remote address. */
    val remoteAddress: SocketAddress

    /** Get the channel's remote port. */
    val remotePort: Int

    /**
     * Connect a client to the remote address. This will allow
     * a ClientNetworkChannel to send and receive data from the
     * remote address.
     * @param remote the targeted remote address.
     */
    fun connect(remote: SocketAddress)
}

/**
 * A SuspendedChannel that accepts new incoming connections.
 */
interface ServerChannel<T : SelectableChannel, C : SelectableChannel> : NetworkChannel<T> {
    /**
     * Accepts an incoming connection and constructs a SuspendedSocketChannel as
     * the connection's interface.
     */
    fun accept(): ClientChannel<C>
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