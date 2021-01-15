package old.ssl

import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executor
import javax.net.ssl.*
import kotlin.math.min

internal class SSLSocketChannelImpl

@Throws(SSLException::class)
constructor(
    /** Source SocketChannel this class wraps. */
    override val channel: SocketChannel,

    /** SSLEngine used for the Secure-Socket communications. */
    private val engine: SSLEngine,

    /** Executor for delegated tasks. */
    private var executor: Executor? = null
) : SSLSocketChannel, ByteChannel by channel {

    /**
     * Application data sent from THIS endpoint.
     */
    private var applicationData: ByteBuffer

    /**
     * Application data received from REMOTE endpoint.
     */
    private var peerApplicationData: ByteBuffer

    /**
     * Encrypted data sent from THIS endpoint.
     */
    private var packetData: ByteBuffer

    /**
     * Encrypted data received from REMOTE endpoint.
     */
    private var peerPacketData: ByteBuffer

    override val session: SSLSession
        get() = engine.session

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

    init {
        engine.beginHandshake()
        // Initialize buffers for decrypted data.
        applicationData = ByteBuffer.allocate(session.applicationBufferSize)
        peerApplicationData = ByteBuffer.allocate(session.applicationBufferSize)
        // Initialize buffers for encrypted data. These will have direct
        // allocation because they will be used for IO operations.
        packetData = ByteBuffer.allocate(session.packetBufferSize)
        peerPacketData = ByteBuffer.allocate(session.packetBufferSize)
    }

    override fun isOpen(): Boolean = channel.isOpen &&
        !engine.isOutboundDone &&
        !engine.isInboundDone

    @Synchronized
    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        applicationData.clear()
        applicationData.put(buffer)
        applicationData.flip()
        var bytesWritten = 0

        while (applicationData.hasRemaining()) {
            packetData.clear()
            val result: SSLEngineResult = engine.wrap(applicationData, packetData)
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                    packetData.flip()
                    while (packetData.hasRemaining()) {
                        bytesWritten += channel.write(packetData)
                    }
                }
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while writing.")
                SSLEngineResult.Status.BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while writing.")
                SSLEngineResult.Status.CLOSED -> {
                    close()
                    return bytesWritten
                }
                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while writing to channel.")
            }
        }

        return bytesWritten
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        if (!buffer.hasRemaining()) {
            return 0
        }

        if (peerApplicationData.hasRemaining()) {
            peerApplicationData.flip()
            return peerApplicationData.copyBytesTo(buffer)
        }

        peerPacketData.compact()

        val bytesRead: Int = channel.read(peerPacketData)

        if (bytesRead > 0 || peerPacketData.hasRemaining()) {
            peerPacketData.flip()
            while (peerPacketData.hasRemaining()) {
                peerApplicationData.compact()
                val result: SSLEngineResult = engine.unwrap(peerPacketData, peerApplicationData)
                when (result.status) {
                    SSLEngineResult.Status.OK -> {
                        peerApplicationData.flip()
                        peerApplicationData.copyBytesTo(buffer)
                    }
                    SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                        peerApplicationData.flip()
                        peerApplicationData.copyBytesTo(buffer)
                    }
                    SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                        peerApplicationData = peerApplicationData.increaseBufferSizeTo(session.applicationBufferSize)
                        read(buffer)
                    }
                    SSLEngineResult.Status.CLOSED -> {
                        close()
                        buffer.clear()
                        return -1
                    }
                    else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                }
            }
        } else if (bytesRead < 0) { // End of stream.
            try {
                engine.closeInbound()
            } catch (e: Exception) {

            }
            close()
            return -1
        }
        return buffer.position()
    }

    @Throws(IOException::class)
    override fun close() {
        engine.closeOutbound()
        try {
            performHandshake()
        } catch (ex: Exception) {

        }
        channel.close()
    }


    @Synchronized
    @Throws(IOException::class)
    override fun performHandshake(): Boolean {
        try {
            var status: SSLEngineResult.HandshakeStatus
            while (engine.handshakeStatus.also { status = it } != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                if (engine.isOutboundDone || engine.isInboundDone) {
                    return false
                }
                when (status) {
                    SSLEngineResult.HandshakeStatus.NEED_WRAP -> wrap()
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> unwrap()
                    SSLEngineResult.HandshakeStatus.NEED_TASK -> runDelegatedTasks()
                    else -> if (peerPacketData.hasRemaining()) { // FINISHED
                        channel.write(peerPacketData)
                    }
                }
            }
            return true
        } catch (ex: SSLException) {
            engine.closeOutbound()
            throw ex
        }
    }

    @Throws(IOException::class)
    private fun wrap() {
        packetData.clear()
        val result: SSLEngineResult = engine.wrap(applicationData, packetData)
        when (result.status!!) {
            SSLEngineResult.Status.BUFFER_UNDERFLOW -> packetData = packetData.increaseBufferSizeTo(session.packetBufferSize)
            SSLEngineResult.Status.BUFFER_OVERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
            else -> writeFromPacketData()
        }
    }

    @Throws(IOException::class)
    private fun unwrap() {
        if (readToPeerPacketData() < 0) {
            return
        }

        do {
            peerPacketData.flip()
            val result: SSLEngineResult = engine.unwrap(peerPacketData, peerApplicationData)
            peerPacketData.compact()

            val retry: Boolean = when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // Check if peer packet data is large enough.
                    if (session.packetBufferSize > peerPacketData.limit()) {
                        println("Increased Buffer Size")
                        val buffer = peerPacketData.increaseBufferSizeTo(session.packetBufferSize)
                        peerPacketData.flip()
                        buffer.put(peerPacketData)
                        peerPacketData = buffer
                    }
                    // Fetch more inbound network data.
                    if (readToPeerPacketData() < 0) {
                        return
                    }
                    // Retry operations
                    true
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    // Resize peer application data.
                    peerApplicationData = peerApplicationData.increaseBufferSizeTo(session.applicationBufferSize)
                    // Retry operations.
                    true
                }
                else -> {
                    if (result.status == SSLEngineResult.Status.CLOSED) {
                        engine.closeOutbound()
                    }
                    false
                }
            }
        } while (retry)
    }

    private fun runDelegatedTasks() {
        var task: Runnable?
        while (engine.delegatedTask.also { task = it } != null) {
            executor?.execute(task!!) ?: task!!.run()
        }
    }

    @Throws(IOException::class)
    private fun readToPeerPacketData(): Int {
        val bytesRead = channel.read(peerPacketData)
        if (bytesRead < 0) {
            engine.closeOutbound()
        }
        return bytesRead
    }

    @Throws(IOException::class)
    private fun writeFromPacketData(): Int {
        packetData.flip()
        val bytesWritten = packetData.position()
        while(packetData.hasRemaining()) {
            channel.write(packetData)
        }
        return bytesWritten
    }

    override fun toString(): String = channel.socket().let { socket ->
        "SSLSocketChannel: ${hashCode()}\n" +
            "Channel Class:     ${channel.javaClass}\n" +
            "Socket Class:      ${socket.javaClass}\n" +
            "Remote Address:    ${socket.remoteSocketAddress}\n" +
            "Remote Port:       ${socket.port}\n" +
            "Local Address:     ${socket.localAddress}\n" +
            "Local Port:        ${socket.localPort}\n" +
            "Need Client Auth:  ${engine.needClientAuth}\n" +
            "Cipher Suit:       ${session.cipherSuite}\n" +
            "Protocol:          ${session.protocol}\n" +
            "Handshake Session: ${engine.handshakeSession}\n" +
            "Handshake Status:  ${engine.handshakeStatus}\n" +
            "SSL Session:       $session"
    }

    companion object {

        /**
         * Compares `sessionProposedCapacity` with buffer's capacity. If buffer's capacity is smaller,
         * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
         * with capacity twice the size of the initial one.
         *
         * @param this - the buffer to be enlarged.
         * @param size - the minimum size of the new buffer, proposed by [SSLSession].
         * @return A new buffer with a larger allocated capacity.
         */
        private fun ByteBuffer.increaseBufferSizeTo(size: Int): ByteBuffer =
            if (size > capacity()) {
                ByteBuffer.allocate(size)
            } else {
                ByteBuffer.allocate(capacity() * 2)
            }


        /**
         * Copy bytes from "this" ByteBuffer to the designated "buffer" ByteBuffer.
         * @param buffer The designated buffer for all bytes to move to.
         * @return number of bytes copied to the other buffer.
         */
        private fun ByteBuffer.copyBytesTo(buffer: ByteBuffer): Int =
            if (remaining() > buffer.remaining()) {
                val limit = min(remaining(), buffer.remaining())
                limit(limit)
                buffer.put(this)
                limit
            } else {
                buffer.put(this)
                remaining()
            }
    }
}