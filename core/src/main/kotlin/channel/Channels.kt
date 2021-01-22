package channel

import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectableChannel

interface NetworkChannel<T : SelectableChannel> {

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

interface ClientNetworkChannel<T : SelectableChannel> : NetworkChannel<T> {
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

interface SuspendedByteChannel {
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