package channel

import java.net.SocketAddress
import java.nio.channels.SelectableChannel

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