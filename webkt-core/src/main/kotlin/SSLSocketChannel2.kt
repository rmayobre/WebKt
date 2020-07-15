import java.io.IOException
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.SSLEngineResult.HandshakeStatus.*
import javax.net.ssl.SSLEngineResult.Status.*
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession

open class SSLSocketChannel2

@Throws(SSLException::class)
constructor(
    private val channel: SocketChannel,
    private val engine: SSLEngine
) : ByteChannel by channel {

    /**
     * Application data received from THIS endpoint.
     */
    protected var applicationData: ByteBuffer

    /**
     * Application data received from OTHER endpoint.
     */
    protected var peerApplicationData: ByteBuffer

    /**
     * Encrypted data received from THIS endpoint.
     */
    protected var packetData: ByteBuffer

    /**
     * Encrypted data received from OTHER endpoint.
     */
    protected var peerPacketData: ByteBuffer

    /**
     * Session created by SSLEngine.
     */
    val session: SSLSession
        get() = engine.session

    val remoteAddress: SocketAddress = channel.remoteAddress

    init {
        // Initialize buffers for decrypted data.
        applicationData = ByteBuffer.allocate(session.applicationBufferSize)
        peerApplicationData = ByteBuffer.allocate(session.applicationBufferSize)
        // Initialize buffers for encrypted data. These will have direct
        // allocation because they will be used for IO operations.
        packetData = ByteBuffer.allocate(session.packetBufferSize)
        peerPacketData = ByteBuffer.allocate(session.packetBufferSize)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        TODO("Read not finished")
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        TODO("Write not finished")
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

    @Throws(ClosedChannelException::class)
    fun register(selector: Selector, operation: Int = SelectionKey.OP_READ): SelectionKey = // TODO not sure if needed.
        channel.register(selector, operation, this)

    @Synchronized
    @Throws(IOException::class)
    fun performHandshake() {
        peerPacketData.clear()
        peerApplicationData.clear()
        applicationData.clear()
        packetData.clear()

        engine.beginHandshake()

        try {
            var status: HandshakeStatus? = engine.handshakeStatus
            loop@ while (status != NOT_HANDSHAKING) {
                status = when (status) {
                    FINISHED -> onFinishedHandshake()
                    NEED_WRAP -> onNeedWrap().handshakeStatus
                    NEED_UNWRAP -> onNeedUnwrap().handshakeStatus
                    NEED_TASK -> onNeedTask()
                    else -> break@loop
                }
            }
        } catch (ex: IOException) {
            engine.closeOutbound()
            throw ex
        }
    }

    @Throws(IOException::class)
    protected fun onFinishedHandshake(): HandshakeStatus {
        if (peerPacketData.hasRemaining()) {
            channel.write(peerPacketData)
        }
        return engine.handshakeStatus
    }

    /**
     * Event called when handshake requires data to be wrapped.
     */
    @Throws(IOException::class)
    protected fun onNeedWrap(): SSLEngineResult {
        val result: SSLEngineResult = engine.wrap(applicationData, packetData)
        when (result.status) {
            OK -> {}
            BUFFER_OVERFLOW -> {}
            BUFFER_UNDERFLOW -> {}
            CLOSED -> {}
            else -> throw IllegalStateException("Invalid SSL status: " + result.status)
        }
        return result
    }

    /**
     * Event called when handshake requires data to be unwrapped.
     */
    @Throws(IOException::class)
    protected fun onNeedUnwrap(): SSLEngineResult {
        if (channel.read(peerPacketData) < 0) {
            TODO("Handle close channel - close engine")
        }

        peerPacketData.flip()
        val result: SSLEngineResult = engine.unwrap(peerPacketData, peerApplicationData)
        peerPacketData.compact()

        when (result.status) {
            OK -> {}
            BUFFER_OVERFLOW -> peerApplicationData = peerApplicationData.enlargeApplicationBuffer()
            BUFFER_UNDERFLOW -> peerPacketData = peerPacketData.handleBufferUnderflow()
            CLOSED -> {}
            else -> throw IllegalStateException("Invalid SSL status: " + result.status)
        }
        return result
    }

    /**
     * Event called when a delegated task from SSL handshake requires handling.
     */
    protected fun onNeedTask(): HandshakeStatus {
        var task: Runnable?
        while (engine.delegatedTask.also { task = it } != null) {
            onHandleDelegatedTask(task!!)
        }
        return engine.handshakeStatus
    }

    /**
     * How a delegated task from the SSL handshake should be handled.
     */
    protected open fun onHandleDelegatedTask(task: Runnable) {
        task.run()
    }

    /**
     * Enlarge a packet buffer. The packet buffer will be sent to the opposing endpoint.
     * @return
     */
    protected fun ByteBuffer.enlargePacketBuffer(): ByteBuffer =
        enlarge(engine.session.packetBufferSize)

    /**
     * Enlarging a packet buffer (peerApplicationData or myAppData)
     *
     * @param buffer the buffer to enlarge
     * @return the enlarged buffer
     */
    protected fun ByteBuffer.enlargeApplicationBuffer(): ByteBuffer =
        enlarge(engine.session.applicationBufferSize)

    companion object {

        /**
         * Compares `sessionProposedCapacity` with buffer's capacity. If buffer's capacity is smaller,
         * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
         * with capacity twice the size of the initial one.
         *
         * @param newCapacity - the minimum size of the new buffer, proposed by [SSLSession].
         * @return A new buffer with a larger capacity.
         */
        private fun ByteBuffer.enlarge(newCapacity: Int): ByteBuffer {
            var buffer: ByteBuffer = this
            buffer = if (newCapacity > buffer.capacity()) {
                ByteBuffer.allocate(newCapacity)
            } else {
                ByteBuffer.allocate(buffer.capacity() * 2)
            }
            return buffer
        }

//        fun createSocket(channel: SocketChannel, engine: SSLEngine,)
    }
}