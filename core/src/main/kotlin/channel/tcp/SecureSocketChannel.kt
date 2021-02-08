package channel.tcp

import channel.tls.TLSChannel
import channel.toString
import copyBytesTo
import increaseBufferSizeTo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.io.IOException
import java.lang.Runnable
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import javax.net.ssl.*
import javax.net.ssl.SSLEngineResult.HandshakeStatus.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

/**
 * TODO this still needs redesign work to make it scale with coroutines better.
 * @param channel
 * @param engine the required SSLEngine
 * @param scope this is a scope for running background tasks. It is not recommended to provided a CoroutineScope for IO operations.
 */
@ObsoleteCoroutinesApi
class SecureSocketChannel(
    channel: SocketChannel,
    private val engine: SSLEngine,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendedSocketChannel(channel, dispatcher), TLSChannel {

    /** Encrypted data sent from THIS endpoint. */
    private var packetData: ByteBuffer

    /** Application data sent from THIS endpoint. */
    private var applicationData: ByteBuffer

    /** Encrypted data received from REMOTE endpoint.*/
    private var peerPacketData: ByteBuffer

    /** Application data received from REMOTE endpoint. */
    private var peerApplicationData: ByteBuffer

    private var operationsActor: SendChannel<Job>

    override val session: SSLSession
        get() = engine.session

    init {
        operationsActor = scope.operationsActor()
        // Initialize buffers for decrypted data.
        applicationData = ByteBuffer.allocate(session.applicationBufferSize)
        peerApplicationData = ByteBuffer.allocate(session.applicationBufferSize)
        // Initialize buffers for encrypted data. These will have direct
        // allocation because they will be used for IO operations.
        packetData = ByteBuffer.allocate(session.packetBufferSize)
        peerPacketData = ByteBuffer.allocate(session.packetBufferSize)
    }

    override val isOpen: Boolean
        get() = super.isOpen &&
                !engine.isOutboundDone &&
                !engine.isInboundDone

    override suspend fun read(buffer: ByteBuffer): Int = suspendCoroutine { continuation ->
        scope.launch {
            operationsActor.send(
                element = launch(start = CoroutineStart.LAZY) {
                    if (!buffer.hasRemaining()) {
                        continuation.resume(0)
                    }

                    if (peerApplicationData.hasRemaining()) {
                        peerApplicationData.flip()
                        continuation.resume(peerApplicationData.copyBytesTo(buffer))
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
                                    continuation.resume(-1)
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
                        continuation.resume(-1)
                    }
                    continuation.resume(buffer.position())
                }
            )
        }
    }

    override suspend fun write(buffer: ByteBuffer): Int = suspendCoroutine { continuation ->
        scope.launch {
            operationsActor.send(
                element = launch(start = CoroutineStart.LAZY) {
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
                                continuation.resume(bytesWritten)
                            }
                            else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while writing to channel.")
                        }
                    }

                    continuation.resume(bytesWritten)
                }
            )
        }
    }

    @ExperimentalCoroutinesApi
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun performHandshake(): Boolean {
        if (operationsActor.isClosedForSend) {
            operationsActor = scope.operationsActor()
        }
        return suspendCoroutine { continuation ->
            scope.launch {
                operationsActor.send(
                    element = launch(start = CoroutineStart.LAZY) {
                        try {
                            var status: SSLEngineResult.HandshakeStatus
                            while (engine.handshakeStatus.also { status = it } != NOT_HANDSHAKING) {
                                if (engine.isOutboundDone || engine.isInboundDone) {
                                    continuation.resume(false)
                                }
                                when (status) {
                                    NEED_WRAP -> wrap()
                                    NEED_UNWRAP -> unwrap()
                                    NEED_TASK -> runDelegatedTasks(engine)
                                    else -> if (peerPacketData.hasRemaining()) { // FINISHED
                                        channel.write(peerPacketData)
                                    }
                                }
                            }
                            continuation.resume(true)
                        } catch (ex: SSLException) {
                            engine.closeOutbound()
                            throw ex
                        }
                    }
                )
            }
        }
    }

    // TODO wrap needs to be redesigned.
    private fun wrap() {
        packetData.clear()
        val result: SSLEngineResult = engine.wrap(applicationData, packetData)
        when (result.status!!) {
            SSLEngineResult.Status.BUFFER_UNDERFLOW -> packetData = packetData.increaseBufferSizeTo(session.packetBufferSize)
            SSLEngineResult.Status.BUFFER_OVERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
            else -> writeFromPacketData()
        }
    }

    private fun writeFromPacketData(): Int {
        packetData.flip()
        val bytesWritten = packetData.position()
        while(packetData.hasRemaining()) {
            channel.write(packetData)
        }
        return bytesWritten
    }

    // TODO unwrap needs to be redesigned.
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

    private fun readToPeerPacketData(): Int {
        val bytesRead = channel.read(peerPacketData)
        if (bytesRead < 0) {
            engine.closeOutbound()
        }
        return bytesRead
    }

    @ExperimentalCoroutinesApi
    override suspend fun close(wait: Boolean) {
        //TODO implement waiting
        scope.launch {
            performHandshake()
            super.close(wait)
        }.invokeOnCompletion {
            engine.closeOutbound()
        }
    }

    override fun toString(): String = toString(engine)

    companion object {

        @ObsoleteCoroutinesApi
        private fun CoroutineScope.operationsActor(): SendChannel<Job> =
            actor(start = CoroutineStart.LAZY) {
                for (job in this.channel) {
                    // Ignore cancelled jobs and jobs that already started.
                    if (!job.isCancelled && job.start()) {
                        job.join()
                    }
                }
            }

        private fun CoroutineScope.runDelegatedTasks(engine: SSLEngine) {
            var task: Runnable?
            while (engine.delegatedTask.also { task = it } != null) {
                launch {
                    task!!.run()
                }
            }
        }

        private suspend inline fun <T> suspendOperations(
            channel: SendChannel<Job>,
            crossinline block: suspend CoroutineScope.(Continuation<T>) -> Unit
        ): T = coroutineScope {
            suspendCoroutine { continuation ->
                launch {
                    channel.send(
                        launch(start = CoroutineStart.LAZY) {
                            block(continuation)
                        }
                    )
                }
            }
        }

        /**
         * Constructs a SuspendedSocketChannel and connect to a remote address.
         *
         * @throws AsynchronousCloseException
         *         If another thread closes this channel
         *         while the connect operation is in progress
         *
         * @throws ClosedByInterruptException
         *         If another thread interrupts the current thread
         *         while the connect operation is in progress, thereby
         *         closing the channel and setting the current thread's
         *         interrupt status
         *
         * @throws UnresolvedAddressException
         *         If the given remote address is not fully resolved
         *
         * @throws UnsupportedAddressTypeException
         *         If the type of the given remote address is not supported
         *
         * @throws SecurityException
         *         If a security manager has been installed
         *         and it does not permit access to the given remote endpoint
         *
         * @throws IOException An I/O related error was thrown
         */
        @Throws(
            AsynchronousCloseException::class,
            ClosedByInterruptException::class,
            UnresolvedAddressException::class,
            UnsupportedAddressTypeException::class,
            SecurityException::class,
            IOException::class
        )
        fun open(
            remote: SocketAddress? = null,
            engine: SSLEngine = SSLContext.getDefault().createSSLEngine(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): SecureSocketChannel = SecureSocketChannel(
            channel = (remote?.let {
                SocketChannel.open(it)
            } ?: SocketChannel.open()).apply {
                configureBlocking(false)
//                connect() TODO make this an async connection - read the connect method.
            },
            engine = engine.apply {
                beginHandshake()
            },
            dispatcher
        )
    }
}