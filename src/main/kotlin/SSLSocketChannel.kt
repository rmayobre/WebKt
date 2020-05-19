import java.io.IOException
import java.lang.IllegalStateException
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

    /**
     * This side's un-encrypted data.
     */
    private val data: ByteBuffer

    /**
     * The decrypted data received from other endpoint.
     */
    private var decryptedPeerData: ByteBuffer

    /**
     * Encrypted data from this side.
     */
    private var encryptedData: ByteBuffer

    /**
     * The encrypted data received from other endpoint.
     */
    private var encryptedPeerData: ByteBuffer

    /**
     * Session created by SSLEngine.
     */
    private val session: SSLSession
        get() = engine.session

    /** Default client-side constructor. */
    constructor(host: String, port: Int): this(SSLContext.getDefault(), host, port)

    /** Client-side constructor. */
    constructor(context: SSLContext, host: String, port: Int): this(SocketChannel.open(), context.createSSLEngine(host, port)) {
        engine.useClientMode = true
    }

    init {
        // Initialize buffers for decrypted data.
        data = ByteBuffer.allocate(session.applicationBufferSize)
        decryptedPeerData = ByteBuffer.allocate(session.applicationBufferSize)
        // Initialize buffers for encrypted data. These will have direct
        // allocation because they will be used for IO operations.
        encryptedData = ByteBuffer.allocateDirect(session.packetBufferSize)
        encryptedPeerData = ByteBuffer.allocateDirect(session.packetBufferSize)
    }

    override fun isOpen(): Boolean = channel.isOpen

    @Throws(IOException::class)
    override fun close() {
        engine.closeOutbound()
        performHandshake()
        channel.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        if (!buffer.hasRemaining()) {
            return 0
        }


        val bytesRead: Int = channel.read(encryptedPeerData)

        if (bytesRead > 0 || encryptedPeerData.hasRemaining()) {
            while (encryptedPeerData.hasRemaining()) {
                decryptedPeerData.compact()
                val result: SSLEngineResult = engine.unwrap(encryptedPeerData, decryptedPeerData)

                return when (result.status) {
                    OK -> {
                        decryptedPeerData.flip()
                        decryptedPeerData.copyBytesTo(buffer)
                    }
                    BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while reading.")
                    BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while reading.")
                    CLOSED -> {
                        close()
                        buffer.clear()
                        -1
                    }
                    else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while reading channel.")
                }
            }
        } else if (bytesRead < 0) {
            engine.closeInbound()
            close()
        }

        return decryptedPeerData.copyBytesTo(buffer)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        var bytesWritten = 0

        while (buffer.hasRemaining()) {
            encryptedData.clear()
            val result: SSLEngineResult = engine.wrap(buffer, encryptedData)
            when (result.status) {
                OK -> {
                    encryptedData.flip()
                    while (encryptedData.hasRemaining()) {
                        bytesWritten += channel.write(encryptedData)
                    }
                }
                BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while writing.")
                BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while writing.")
                CLOSED -> {
                    close()
                    return bytesWritten
                }
                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while writing to channel.")
            }
        }

        return bytesWritten
    }

    /**
     * Connect channel to an address. Only use this function if the channel is
     * intended to be used for client-side operations.
     * @param address the address for the channel to connect to.
     * @param block configure the channel for blocking or non-blocking operations.
     * @return returns true if channel connected, otherwise false.
     */
    @Synchronized
    @Throws(IOException::class)
    fun connect(address: InetSocketAddress, block: Boolean = false): Boolean {
        if (!channel.isConnected) {
            channel.configureBlocking(block)
            if (channel.connect(address)) {
                engine.beginHandshake()
                if (!performHandshake()) {
                    close()
                }
            }
        } else {
            return true
        }

        return false
    }

    @Synchronized
    @Throws(IOException::class)
    fun performHandshake(): Boolean {
        var isComplete = false
        while (!isComplete && isOpen) {
            when (engine.handshakeStatus) {
                FINISHED -> isComplete = if (encryptedPeerData.hasRemaining()) {
                    channel.write(encryptedPeerData)
                    false
                } else {
                    true
                }
                NEED_WRAP -> {
                    // If could not wrap, then handshake fails.
                    if (!wrap()) {
                        return false
                    }
                }
                NEED_UNWRAP -> {
                    // If could not unwrap, then handshake fails.
                    if (!unwrap()) {
                        return false
                    }
                }
                NEED_TASK -> {
                    var task: Runnable?
                    while (engine.delegatedTask.also { task = it } != null) {
                        task?.run() // TODO run asynchronously
                    }
                }
                NOT_HANDSHAKING -> return false
                else -> throw IllegalStateException("SSLEngine handshake status: ${engine.handshakeStatus}, could not be determined.")
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun wrap(): Boolean {
        encryptedData.clear()
        try {
            val result: SSLEngineResult = engine.wrap(data, encryptedData)
            return when (result.status) {
                OK -> {
                    encryptedData.flip()
                    while (encryptedData.hasRemaining()) {
                        channel.write(encryptedData)
                    }
                    true
                }
                BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
                BUFFER_OVERFLOW -> {
                    encryptedData = if (encryptedData.capacity() < session.packetBufferSize) {
                        ByteBuffer.allocate(session.packetBufferSize)
                    } else {
                        ByteBuffer.allocate(encryptedData.capacity() * 2)
                    }
                    true
                }
                CLOSED -> try {
                    encryptedData.flip()
                    while (encryptedData.hasRemaining()) {
                        channel.write(encryptedData)
                    }
                    encryptedPeerData.clear()
                    true
                } catch (ex: IOException) {
                    false
                }
                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while wrapping.")
            }

        } catch (ex: SSLException) {
            engine.closeOutbound()
            return false
        }
    }

    @Throws(IOException::class)
    private fun unwrap(): Boolean {
        try {
            if (channel.read(encryptedPeerData) < 0) {
                if (engine.isInboundDone && engine.isOutboundDone) {
                    return false
                }
                engine.closeInbound()
                engine.closeOutbound()
                return false
            }

            encryptedPeerData.flip()
            val result: SSLEngineResult = engine.wrap(encryptedPeerData, decryptedPeerData)
            encryptedPeerData.compact()

            return when (result.status) {
                OK -> true
                BUFFER_UNDERFLOW -> {
                    if (encryptedPeerData.limit() <= session.packetBufferSize) {
                        val buffer: ByteBuffer = if (encryptedPeerData.capacity() < session.packetBufferSize) {
                            ByteBuffer.allocateDirect(session.packetBufferSize)
                        } else {
                            ByteBuffer.allocateDirect(encryptedPeerData.capacity() * 2)
                        }
                        encryptedPeerData.flip()
                        buffer.put(encryptedPeerData)
                        encryptedPeerData = buffer
                    }
                    true
                }
                BUFFER_OVERFLOW -> {
                    decryptedPeerData = if (decryptedPeerData.capacity() < session.applicationBufferSize) {
                        ByteBuffer.allocate(session.applicationBufferSize)
                    } else {
                        ByteBuffer.allocate(decryptedPeerData.capacity() * 2)
                    }
                    true
                }
                CLOSED -> {
                    if (engine.isOutboundDone) {
                        false
                    } else {
                        engine.closeOutbound()
                        true
                    }
                }
                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while unwrapping.")
            }
        } catch (ex: SSLException) {
            engine.closeOutbound()
            return false
        }
    }

    companion object {

        /**
         * Copy bytes from "this" ByteBuffer to the designated "buffer" ByteBuffer.
         * @param buffer The designated buffer for all bytes to move to.
         * @return number of bytes copied to the other buffer.
         */
        private fun ByteBuffer.copyBytesTo(buffer: ByteBuffer): Int = if (remaining() > buffer.remaining()) {
            val diff: Int = remaining() - buffer.remaining()
            limit(diff)
            buffer.put(this)
            diff
        } else {
            buffer.put(this)
            remaining()
        }

    }
}