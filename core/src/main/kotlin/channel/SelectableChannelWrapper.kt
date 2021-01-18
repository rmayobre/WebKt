package channel

import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.SelectableChannel
import javax.net.ssl.SSLSession

interface SelectableChannelWrapper {

    /** The channel being wrapped. */
    val channel: SelectableChannel

    /** Channel's SSLSession created from SSLEngine. */
    val session: SSLSession

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