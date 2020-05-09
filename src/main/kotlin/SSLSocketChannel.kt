import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*

class SSLSocketChannel(
    private val channel: SocketChannel
) : ByteChannel by channel,
    ScatteringByteChannel by channel,
    GatheringByteChannel by channel,
    NetworkChannel by channel {

    constructor(address: SocketAddress): this(

    )

    override fun bind(address: SocketAddress): SSLSocketChannel {
        channel.bind(address)
        return this
    }

    override fun isOpen(): Boolean = channel.isOpen

    override fun read(buffer: ByteBuffer): Int = channel.read(buffer)

    override fun write(buffer: ByteBuffer): Int = channel.write(buffer)

    override fun close() = channel.close()
}

//class SSLSocketChannel(
//    private val channel: SocketChannel
//) : SocketChannel(channel.provider()) {
//
//    override fun bind(p0: SocketAddress): SocketChannel = channel.bind(p0)
//
//    override fun socket(): Socket = channel.socket()
//
//    override fun connect(p0: SocketAddress?): Boolean = channel.connect(p0)
//
//    override fun isConnected(): Boolean = channel.isConnected
//
//    override fun isConnectionPending(): Boolean = channel.isConnectionPending
//
//    override fun finishConnect(): Boolean = channel.finishConnect()
//
//    override fun getLocalAddress(): SocketAddress = channel.localAddress
//
//    override fun getRemoteAddress(): SocketAddress = channel.remoteAddress
//
//    override fun read(buffer: ByteBuffer): Int = channel.read(buffer)
//
//    override fun read(p0: Array<out ByteBuffer>, p1: Int, p2: Int): Long = channel.read(p0, p1, p2)
//
//    override fun write(buffer: ByteBuffer): Int = channel.write(buffer)
//
//    override fun write(p0: Array<out ByteBuffer>?, p1: Int, p2: Int): Long = channel.write(p0, p1, p2)
//
//    override fun supportedOptions(): MutableSet<SocketOption<*>> = channel.supportedOptions()
//
//    override fun <T : Any?> getOption(p0: SocketOption<T>): T = channel.getOption(p0)
//
//    override fun <T : Any?> setOption(p0: SocketOption<T>, p1: T): SocketChannel = channel.setOption(p0, p1)
//
//    override fun implConfigureBlocking(p0: Boolean) {}
//
//    override fun implCloseSelectableChannel() {}
//
//    override fun shutdownOutput(): SocketChannel = channel.shutdownOutput()
//
//    override fun shutdownInput(): SocketChannel = channel.shutdownInput()
//}