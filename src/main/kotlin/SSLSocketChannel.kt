import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession

class SSLSocketChannel(
    private val channel: SocketChannel,
    private val sslEngine: SSLEngine
) : ByteChannel, NetworkChannel by channel {

    private val session: SSLSession = sslEngine.session

    /**
     * This side's un-encrypted data.
     */
    private lateinit var data: ByteBuffer

    /**
     * Encrypted data from this side.
     */
    private lateinit var encryptedData: ByteBuffer

    /**
     * This will contain the endpoint's decrypted data.
     */
    private lateinit var peerData: ByteBuffer

    /**
     * Contains the encrypted data sent from endpoint.
     */
    private lateinit var encryptedPeerData: ByteBuffer

    /** Client-side constructor. */
    constructor(context: SSLContext): this(SocketChannel.open(), context.createSSLEngine()) {
        sslEngine.useClientMode = true
    }

    /** Server-side constructor. */
    constructor(channel: SocketChannel, context: SSLContext): this(channel, context.createSSLEngine()) {
        TODO("perform handshake.")
    }

    override fun isOpen(): Boolean = channel.isOpen

    override fun read(buffer: ByteBuffer): Int {
        channel.read(buffer)
        TODO("Use SSLEngine to encrypt data.")
    }

    override fun write(buffer: ByteBuffer): Int {
        channel.write(buffer)
        TODO("Use SSLEngine to encrypt data.")
    }

    override fun close() = channel.close()

    @Throws(IOException::class)
    fun connect(address: InetSocketAddress, block: Boolean = false): Boolean {
        if (!channel.isConnected) {
            channel.configureBlocking(block)
            if (channel.connect(address)) {
                sslEngine.beginHandshake()
                TODO("FINISH CONNECTION PROCESS.")
            }
        }

        return false
    }

    private fun performHandshake() {
        TODO("Process handshake.")
    }
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