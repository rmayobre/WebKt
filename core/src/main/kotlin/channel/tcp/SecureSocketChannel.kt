package channel.tcp

import channel.tls.HandshakeResult
import channel.tls.TLSChannel
import channel.toString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Runnable
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSession
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @param channel
 * @param engine the required SSLEngine
 * @param scope this is a scope for running background tasks. It is not recommended to provided a CoroutineScope for IO operations.
 */
class SecureSocketChannel(
    channel: SocketChannel,
    private val engine: SSLEngine,
    private val scope: CoroutineScope
) : SuspendedSocketChannel(channel), TLSChannel {

    /** Encrypted data sent from THIS endpoint. */
    private var packetData: ByteBuffer

    /** Application data sent from THIS endpoint. */
    private var applicationData: ByteBuffer

    /** Encrypted data received from REMOTE endpoint.*/
    private var peerPacketData: ByteBuffer

    /** Application data received from REMOTE endpoint. */
    private var peerApplicationData: ByteBuffer

    private lateinit var deferredOperationsChannel: SendChannel<Job>

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
        suspendCoroutine { continuation ->
            launch {
                deferredOperationsChannel.send(
                    element = launch(start = CoroutineStart.LAZY) {
                        continuation.resumeWithException(TODO("Finish logic for read"))
                    }
                )
            }
        }
    }

    override suspend fun write(buffer: ByteBuffer): Int = coroutineScope {
        suspendCoroutine { continuation ->
            launch {
                deferredOperationsChannel.send(
                    element = launch(start = CoroutineStart.LAZY) {
                        continuation.resumeWithException(TODO("Finish logic for write"))
                    }
                )
            }
        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override suspend fun performHandshake(): HandshakeResult {
        if (deferredOperationsChannel.isClosedForSend) {
            deferredOperationsChannel = scope.actor {
                for (job in this.channel) {
                    job.join()
                }
            }
        }
        return coroutineScope {
            suspendCoroutine { continuation ->
                launch {
                    deferredOperationsChannel.send(
                        element = launch(start = CoroutineStart.LAZY) {
                            continuation.resumeWithException(TODO("Finish logic for handshake"))
                        }
                    )
                }
            }

//            val handshake = async<> {
//                TODO("Write handshake process.")
//            }
//            deferredOperationsChannel.send(handshake)
//            return handshake.await()
        }
    }

    private suspend fun wrap() = coroutineScope {

    }

    private suspend fun unwrap() = coroutineScope {

    }

    override fun close() = try {
        //performHandshake() TODO implement handshake
            // TODO close the coroutine channels (actors)
        channel.close()
    } finally {
        engine.closeOutbound()
    }

    override fun toString(): String = toString(engine)

    companion object {

        private fun CoroutineScope.runDelegatedTasks(engine: SSLEngine) {
            var task: Runnable?
            while (engine.delegatedTask.also { task = it } != null) {
                launch {
                    task!!.run()
                }
            }
        }


        /*
        TODO create suspended helper functions?

         */
        fun open(
            engine: SSLEngine,
            scope: CoroutineScope
        ): SecureSocketChannel = open(
            channel = SocketChannel.open(),
            engine,
            scope
        )

        fun open(
            remote: SocketAddress,
            engine: SSLEngine,
            scope: CoroutineScope
        ): SecureSocketChannel = open(
            channel = SocketChannel.open(remote),
            engine,
            scope
        )

        fun open(
            channel: SocketChannel,
            engine: SSLEngine,
            scope: CoroutineScope
        ): SecureSocketChannel = SecureSocketChannel(
            channel = channel.apply {
                configureBlocking(false)
            },
            engine = engine.apply {
                beginHandshake()
            },
            scope
        )
    }
}