package tls

import ByteBufferChannel
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession
import kotlin.math.min

class SecureSocketChannel(
    val channel: SocketChannel,
    /** SSLEngine used for the Secure-Socket communications. */
    private val engine: SSLEngine
) : TLSChannel, ByteBufferChannel {

    private lateinit var datagramChannel: DatagramChannel

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

    /** Channel's SSLSession created from SSLEngine. */
    val session: SSLSession
        get() = engine.session

    /** Get the socket's InetAddress */
    val inetAddress: InetAddress
        get() = channel.socket().inetAddress

    /** Get the channel's remote address. */
    val remoteAddress: SocketAddress
        get() = channel.remoteAddress

    /** Get the channel's remote port. */
    val remotePort: Int
        get() = channel.socket().port

    /** Get the channel's local address. */
    val localAddress: SocketAddress
        get() = channel.localAddress

    /** Get the channel's local port. */
    val localPort: Int
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


    override suspend fun performHandshake(): HandshakeResult {
        TODO("Not yet implemented")
    }

    override suspend fun read(buffer: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override suspend fun write(buffer: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    override fun close() {
        engine.closeOutbound()
        try {
            //performHandshake() TODO implement handshake
        } catch (ex: Exception) {

        }
        channel.close()
    }

    override fun toString(): String = channel.socket().let { socket ->
        "SecureSocketChannel: ${hashCode()}\n" +
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