package channel

import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.Channel
import java.nio.channels.SelectableChannel
import javax.net.ssl.SSLSession

interface NetworkChannel<T : SelectableChannel> {

    /** The channel being wrapped. */
    val channel: T

    /** Get the socket's InetAddress */
    val inetAddress: InetAddress

    /** Get the channel's remote address. */
    val remoteAddress: SocketAddress

    /** Get the channel's remote port. */
    val remotePort: Int

    /** Get the channel's local address. */
    val localAddress: SocketAddress

    /** Get the channel's local port. */
    val localPort: Int
}