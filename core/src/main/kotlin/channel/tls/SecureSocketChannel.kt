package channel.tls

import channel.ByteBufferChannel
import channel.SelectableChannelWrapper
import channel.SuspendedBufferChannel
import channel.toString
import kotlinx.coroutines.*
import java.lang.Runnable
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession
import kotlin.math.min

class SecureSocketChannel(
    override val channel: SocketChannel,
    /** SSLEngine used for the Secure-Socket communications. */
    private val engine: SSLEngine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TLSChannel, SelectableChannelWrapper, SuspendedBufferChannel {

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
    override val session: SSLSession
        get() = engine.session

    /** Get the socket's InetAddress */
    override val inetAddress: InetAddress
        get() = channel.socket().inetAddress

    /** Get the channel's remote address. */
    override val remoteAddress: SocketAddress
        get() = channel.remoteAddress

    /** Get the channel's remote port. */
    override val remotePort: Int
        get() = channel.socket().port

    /** Get the channel's local address. */
    override val localAddress: SocketAddress
        get() = channel.localAddress

    /** Get the channel's local port. */
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

    override suspend fun read(buffer: ByteBuffer): Int { // Should this read into a flow?
        TODO("Not yet implemented")
    }

    override suspend fun write(buffer: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override suspend fun performHandshake(): HandshakeResult {
        TODO("Not yet implemented")
    }

    private suspend fun wrap() = coroutineScope {

    }

    private suspend fun unwrap() = coroutineScope {

    }

    private suspend fun runDelegatedTasks() = coroutineScope {
        var task: Runnable?
        while (engine.delegatedTask.also { task = it } != null) {
            launch(dispatcher) {
                task!!.run()
            }
        }
    }

    override fun close() = try {
        //performHandshake() TODO implement handshake
        channel.close()
    } finally {
        engine.closeOutbound()
    }

    override fun toString(): String =
        toString(engine)

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