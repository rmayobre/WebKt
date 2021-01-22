package channel.tcp

import channel.tls.HandshakeResult
import channel.tls.TLSChannel
import channel.toString
import kotlinx.coroutines.*
import java.lang.Runnable
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession
import kotlin.math.min

/**
 * @param channel
 * @param engine the required SSLEngine
 * @param dispatcher the dispatcher to run delegated tasks for the SSL process.
 */
class SecureSocketChannel(
    channel: SocketChannel,
    private val engine: SSLEngine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : SuspendedSocketChannel(channel), TLSChannel {

    /** Encrypted data sent from THIS endpoint. */
    private var packetData: ByteBuffer

    /** Application data sent from THIS endpoint. */
    private var applicationData: ByteBuffer

    /** Encrypted data received from REMOTE endpoint.*/
    private var peerPacketData: ByteBuffer

    /** Application data received from REMOTE endpoint. */
    private var peerApplicationData: ByteBuffer

    override val session: SSLSession
        get() = engine.session

    constructor(engine: SSLEngine): this(SocketChannel.open(), engine)

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

    override fun isOpen(): Boolean =
        channel.isOpen &&
        !engine.isOutboundDone &&
        !engine.isInboundDone


    override suspend fun read(buffer: ByteBuffer): Int = coroutineScope {
        super.read(buffer)
        TODO("Not yet implemented")
    }

    override suspend fun write(buffer: ByteBuffer): Int = coroutineScope {
        super.write(buffer)
        TODO("Not yet implemented")
    }

    override suspend fun performHandshake(): HandshakeResult = coroutineScope {
        TODO("Not yet implemented")
    }

    private suspend fun wrap() = coroutineScope {

    }

    private suspend fun unwrap() = coroutineScope {

    }

    override fun close() = try {
        //performHandshake() TODO implement handshake
        channel.close()
    } finally {
        engine.closeOutbound()
    }

    override fun toString(): String = toString(engine)

    companion object {

        private suspend fun SSLEngine.runDelegatedTasks(
            dispatcher: CoroutineDispatcher
        ) = coroutineScope {
            var task: Runnable?
            while (delegatedTask.also { task = it } != null) {
                launch(dispatcher) {
                    task!!.run()
                }
            }
        }


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