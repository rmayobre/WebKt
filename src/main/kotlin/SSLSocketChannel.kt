import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.NetworkChannel
import java.nio.channels.SocketChannel
import javax.net.ssl.*
import javax.net.ssl.SSLEngineResult.HandshakeStatus.*
import javax.net.ssl.SSLEngineResult.Status.*

class SSLSocketChannel(
    private val channel: SocketChannel,
    private val engine: SSLEngine
) : ByteChannel, NetworkChannel by channel {

    private val session: SSLSession = engine.session

    private var isShakingHands = true

    /**
     * This side's un-encrypted data.
     */
    private lateinit var decryptedData: ByteBuffer

    /**
     * Encrypted data from this side.
     */
    private lateinit var encryptedData: ByteBuffer

    /**
     * The decrypted data received from other endpoint.
     */
    private lateinit var decryptedPeerData: ByteBuffer

    /**
     * The encrypted data received from other endpoint.
     */
    private lateinit var encryptedPeerData: ByteBuffer

    /** Default client-side constructor. */
    constructor(host: String, port: Int): this(SSLContext.getDefault(), host, port)

    /** Client-side constructor. */
    constructor(context: SSLContext, host: String, port: Int): this(SocketChannel.open(), context.createSSLEngine(host, port)) {
        engine.useClientMode = true
    }

    /** Server-side constructor. */
    constructor(channel: SocketChannel, context: SSLContext): this(channel, context.createSSLEngine()) {
        engine.useClientMode = false
        engine.needClientAuth = true

        TODO("perform handshake.")
    }

    init {

    }

    override fun isOpen(): Boolean = channel.isOpen

    @Throws(IOException::class)
    override fun close() = channel.close()

    @Synchronized
    @Throws(IOException::class)
    fun connect(address: InetSocketAddress, block: Boolean = false): Boolean {
        if (!channel.isConnected) {
            channel.configureBlocking(block)
            if (channel.connect(address)) {
                engine.beginHandshake()
                TODO("FINISH CONNECTION PROCESS.")
            }
        }

        return false
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        channel.read(buffer)
        TODO("Use SSLEngine to encrypt data.")
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        channel.write(buffer)
        TODO("Use SSLEngine to encrypt data.")
    }

    private fun performHandshake() {
        while (isShakingHands && isOpen) {
            when (engine.handshakeStatus!!) {
                NOT_HANDSHAKING -> {
                    if (encryptedData.position() > 0) {
                        isShakingHands = isShakingHands or wrap()
                    }
                    if (decryptedData.position() > 0) {
                        isShakingHands = isShakingHands or unwrap()
                    }
                }
                NEED_WRAP -> isShakingHands = wrap()
                NEED_UNWRAP -> isShakingHands = unwrap()
                NEED_TASK -> {
                    var task: Runnable?
                    while (engine.delegatedTask.also { task = it } != null) {
                        task?.run() // TODO run asynchronously
                    }
                    isShakingHands = true
                }
                FINISHED -> isShakingHands = false
            }
        }
    }

    @Throws(SSLException::class)
    private fun wrap(): Boolean {
        val result: SSLEngineResult = try {
            encryptedData.flip()
            engine.wrap(encryptedData, encryptedPeerData)
        } catch (ex: SSLException) {
            engine.closeOutbound()
            return false
        } finally {
            encryptedData.compact()
        }

        when (result.status!!) {
            OK -> {
                if (encryptedPeerData.position() > 0) {
                    encryptedPeerData.flip()
                    channel.write(encryptedPeerData)
                    encryptedPeerData.compact()
                }
            }
            BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
            BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while wrapping.")
            CLOSED -> return false // Operations closed.
        }

        return true
    }

    @Throws(SSLException::class)
    private fun unwrap(): Boolean {
        val result: SSLEngineResult = try {
            decryptedData.flip()
            engine.wrap(decryptedData, decryptedPeerData)
        } catch (ex: SSLException) {
            engine.closeOutbound()
            return false
        } finally {
            decryptedData.compact()
        }

        when (result.status!!) {
            OK -> {
                if (decryptedPeerData.position() > 0) {
                    decryptedPeerData.flip()
                    channel.write(decryptedPeerData)
                    decryptedPeerData.compact()
                }
            }
            BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
            BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while wrapping.")
            CLOSED -> return false // Operations closed.
        }

        if (result.handshakeStatus == FINISHED) {
            return false
        }

        return true
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