package channel

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.Channel

open class SuspendedSocketChannel(
    override val channel: SocketChannel
) : SuspendedByteChannel, NetworkChannel<SocketChannel>, Channel by channel {

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