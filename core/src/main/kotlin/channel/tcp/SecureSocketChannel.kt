package channel.tcp

import channel.tls.HandshakeResult
import channel.tls.TLSChannel
import channel.toString
import kotlinx.coroutines.*
import java.lang.Runnable
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession

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

    init {
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

        fun open(
            engine: SSLEngine,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ): SecureSocketChannel = open(
            channel = SocketChannel.open(),
            engine, dispatcher
        )

        fun open(
            remote: SocketAddress,
            engine: SSLEngine,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ): SecureSocketChannel = open(
            channel = SocketChannel.open(remote),
            engine, dispatcher
        )

        fun open(
            channel: SocketChannel,
            engine: SSLEngine,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ): SecureSocketChannel = SecureSocketChannel(
            channel = channel.apply {
                configureBlocking(false)
            },
            engine = engine.apply {
                beginHandshake()
            },
            dispatcher
        )
    }
}