import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult
import javax.net.ssl.SSLEngineResult.HandshakeStatus.*
import javax.net.ssl.SSLEngineResult.Status.*
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession


open class SSLSocketChannel

@Throws(SSLException::class)
constructor(
    private val channel: SocketChannel,
    private val engine: SSLEngine
) : ByteChannel by channel {

    /**
     * Application data received from THIS endpoint.
     */
    private val applicationData: ByteBuffer

    /**
     * Application data received from OTHER endpoint.
     */
    private var peerApplicationData: ByteBuffer

    /**
     * Encrypted data received from THIS endpoint.
     */
    private var packetData: ByteBuffer

    /**
     * Encrypted data received from OTHER endpoint.
     */
    private var peerPacketData: ByteBuffer

    /**
     * Session created by SSLEngine.
     */
    private val session: SSLSession
        get() = engine.session

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

    @Synchronized
    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        var bytesWritten = 0

        while (buffer.hasRemaining()) {
            peerApplicationData.clear()
            val result: SSLEngineResult = engine.wrap(buffer, peerApplicationData)
            when (result.status) {
                OK -> {
                    peerApplicationData.flip()
                    while (peerApplicationData.hasRemaining()) {
                        bytesWritten += channel.write(peerApplicationData)
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

    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        if (!buffer.hasRemaining()) {
            return 0
        }


        val bytesRead: Int = channel.read(peerPacketData)

        if (bytesRead > 0 || peerPacketData.hasRemaining()) {
            while (peerPacketData.hasRemaining()) {
                packetData.compact()
                val result: SSLEngineResult = engine.unwrap(peerPacketData, packetData)

                return when (result.status) {
                    OK -> {
                        packetData.flip()
                        packetData.copyBytesTo(buffer)
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

        return packetData.copyBytesTo(buffer)
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

    @Throws(IOException::class)
    fun performHandshake(): Boolean {
        peerPacketData.clear()
        peerApplicationData.clear()
        applicationData.clear()
        packetData.clear()

        try {
            var status: SSLEngineResult.HandshakeStatus? = engine.handshakeStatus
            loop@ while (status != NOT_HANDSHAKING) {
                status = when (status) {
                    NEED_WRAP -> onNeedWrap()
                    NEED_UNWRAP -> onNeedUnwrap()
                    NEED_TASK -> onNeedTask()
                    else -> if (status == FINISHED && peerPacketData.hasRemaining()) {
                        channel.write(peerPacketData)
                        engine.handshakeStatus
                    } else {
                        break@loop
                    }
                }
            }
        } catch (ex: SSLException) {
            engine.closeOutbound()
            throw ex
        }
    }

    /**
     * Event called when handshake requires data to be wrapped.
     */
    @Throws(SSLException::class)
    private fun onNeedWrap(): SSLEngineResult.HandshakeStatus {
        packetData.clear()
        val result: SSLEngineResult = engine.wrap(applicationData, packetData)

        when (result.status!!) {
            OK -> {
                packetData.flip()
                while (packetData.hasRemaining()) {
                    channel.write(packetData)
                }
            }
            BUFFER_OVERFLOW -> {
                packetData = if (packetData.capacity() < session.packetBufferSize) {
                    ByteBuffer.allocate(session.packetBufferSize)
                } else {
                    ByteBuffer.allocate(packetData.capacity() * 2)
                }
            }
            BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
            CLOSED -> {
                packetData.flip()
                while (packetData.hasRemaining()) {
                    channel.write(packetData)
                }
                packetData.clear()
            }
        }

        return result.handshakeStatus
    }

    /**
     * Event called when handshake requires data to be unwrapped.
     */
    @Throws(SSLException::class)
    private fun onNeedUnwrap(): SSLEngineResult.HandshakeStatus {
        if (channel.read(peerPacketData) < 0) {
            engine.closeOutbound()
            return engine.handshakeStatus
        }

        peerPacketData.flip()
        val result: SSLEngineResult = engine.unwrap(peerPacketData, peerApplicationData)
        peerPacketData.compact()

        when (result.status!!) {
            OK -> {
//                println("OK")
                //TODO("The SSLEngine completed the operation, and is available to process similar calls.")
            }
            BUFFER_OVERFLOW -> {
                // Enlarge peer packet buffer.
                peerApplicationData = if (peerApplicationData.capacity() < session.applicationBufferSize) {
//                    ByteBuffer.allocate(session.applicationBufferSize)
                    enlargeBuffer(peerApplicationData, session.applicationBufferSize)
                } else {
                    enlargeBuffer(peerApplicationData, session.applicationBufferSize + peerApplicationData.position())
                }
            }
            BUFFER_UNDERFLOW -> {
                // Enlarge peer application buffer
                if (peerApplicationData.capacity() < session.packetBufferSize) {
//                    println("resize source buffer up to ${session.packetBufferSize} bytes for BUFFER_UNDERFLOW")
                    peerPacketData = enlargeBuffer(peerPacketData, session.packetBufferSize)
                }
//                if (peerPacketData.limit() <= session.packetBufferSize) {
//                    val buffer: ByteBuffer = if (peerPacketData.capacity() < session.packetBufferSize) {
//                        ByteBuffer.allocateDirect(session.packetBufferSize)
//                    } else {
//                        ByteBuffer.allocateDirect(peerPacketData.capacity() * 2)
//                    }
//                    peerPacketData.flip()
//                    buffer.put(peerPacketData)
//                    peerPacketData = buffer
//                }
            }
            CLOSED -> {
                if (!engine.isOutboundDone) {
                    engine.closeOutbound()
                    return engine.handshakeStatus
                }
            }
        }

        return result.handshakeStatus
    }

    private fun onNeedTask(): SSLEngineResult.HandshakeStatus {
        var task: Runnable?
        while (engine.delegatedTask.also { task = it } != null) {
            onHandleDelegatedTask(task!!)
        }
        return engine.handshakeStatus
    }

    /**
     * Event called when a delegated task from SSL handshake requires handling.
     */
    protected open fun onHandleDelegatedTask(task: Runnable) {
        task.run()
    }

//    @Synchronized
//    @Throws(IOException::class)
//    fun performHandshake(): Boolean {
//        engine.beginHandshake()
//        var isComplete = false
//        while (!isComplete) {
//            when (engine.handshakeStatus) {
//                FINISHED -> isComplete = if (encryptedPeerData.hasRemaining()) {
//                    channel.write(encryptedPeerData)
//                    false
//                } else {
//                    true
//                }
//                NEED_WRAP -> {
//                    // If could not wrap, then handshake fails.
//                    if (!wrap()) {
//                        return false
//                    }
//                }
//                NEED_UNWRAP -> {
//                    // If could not unwrap, then handshake fails.
//                    if (!unwrap()) {
//                        return false
//                    }
//                }
//                NEED_TASK -> {
//                    var task: Runnable?
//                    while (engine.delegatedTask.also { task = it } != null) {
//                        task?.run()
//                    }
//                }
//                NOT_HANDSHAKING -> return false
//                else -> throw IllegalStateException("SSLEngine handshake status: ${engine.handshakeStatus}, could not be determined.")
//            }
//        }
//        return true
//    }
//
//    @Throws(IOException::class)
//    protected fun wrap(): Boolean {
//        encryptedData.clear()
//        try {
//            val result: SSLEngineResult = engine.wrap(data, encryptedData)
//            return when (result.status) {
//                OK -> {
//                    encryptedData.flip()
//                    while (encryptedData.hasRemaining()) {
//                        channel.write(encryptedData)
//                    }
//                    true
//                }
//                BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while wrapping.")
//                BUFFER_OVERFLOW -> {
//                    encryptedData = if (encryptedData.capacity() < session.packetBufferSize) {
//                        ByteBuffer.allocate(session.packetBufferSize)
//                    } else {
//                        ByteBuffer.allocate(encryptedData.capacity() * 2)
//                    }
//                    true
//                }
//                CLOSED -> try {
//                    encryptedData.flip()
//                    while (encryptedData.hasRemaining()) {
//                        channel.write(encryptedData)
//                    }
//                    encryptedPeerData.clear()
//                    true
//                } catch (ex: IOException) {
//                    false
//                }
//                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while wrapping.")
//            }
//
//        } catch (ex: SSLException) {
//            engine.closeOutbound()
//            return false
//        }
//    }
//
//    /**
//     *
//     * @return Returns false if unwrap process failed.
//     * @throws IOException if there was an issue reading channel.
//     */
//    @Throws(IOException::class)
//    protected fun unwrap(): Boolean {
//        try {
//            if (channel.read(encryptedPeerData) < 0) {
//                if (engine.isInboundDone && engine.isOutboundDone) {
//                    return false
//                }
//                engine.closeInbound()
//                engine.closeOutbound()
//                return true
//            }
//
//            encryptedPeerData.flip()
//            val result: SSLEngineResult = engine.unwrap(encryptedPeerData, decryptedPeerData)
//            encryptedPeerData.compact()
//
//            return when (result.status) {
//                OK -> true
//                BUFFER_UNDERFLOW -> {
//                    if (encryptedPeerData.limit() <= session.packetBufferSize) {
//                        val buffer: ByteBuffer = if (encryptedPeerData.capacity() < session.packetBufferSize) {
//                            ByteBuffer.allocateDirect(session.packetBufferSize)
//                        } else {
//                            ByteBuffer.allocateDirect(encryptedPeerData.capacity() * 2)
//                        }
//                        encryptedPeerData.flip()
//                        buffer.put(encryptedPeerData)
//                        encryptedPeerData = buffer
//                    }
//                    true
//                }
//                BUFFER_OVERFLOW -> {
//                    decryptedPeerData = if (decryptedPeerData.capacity() < session.applicationBufferSize) {
//                        ByteBuffer.allocate(session.applicationBufferSize)
//                    } else {
//                        ByteBuffer.allocate(decryptedPeerData.capacity() * 2)
//                    }
//                    true
//                }
//                CLOSED -> {
//                    if (engine.isOutboundDone) {
//                        false
//                    } else {
//                        engine.closeOutbound()
//                        true
//                    }
//                }
//                else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while unwrapping.")
//            }
//        } catch (ex: SSLException) {
//            engine.closeOutbound()
//            return false
//        }
//    }

    companion object {

        private fun enlargeBuffer(buffer: ByteBuffer, size: Int): ByteBuffer {
            val bb = ByteBuffer.allocate(size)
            buffer.flip()
            bb.put(buffer)
            return bb
        }


        /**
         * Copy bytes from "this" ByteBuffer to the designated "buffer" ByteBuffer.
         * @param buffer The designated buffer for all bytes to move to.
         * @return number of bytes copied to the other buffer.
         */
        protected fun ByteBuffer.copyBytesTo(buffer: ByteBuffer): Int = if (remaining() > buffer.remaining()) {
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