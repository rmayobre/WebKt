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


open class SSLSocketChannel

@Throws(SSLException::class)
constructor(
    private val channel: SocketChannel,
    private val engine: SSLEngine
) : ByteChannel by channel {

    /**
     * Application data received from THIS endpoint.
     */
    private var applicationData: ByteBuffer

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

    val remoteAddress: SocketAddress = channel.remoteAddress

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

    // Should this channel be considered closed if engine is outbound and inbound done?
//    override fun isOpen(): Boolean = channel.isOpen && engine.is

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
        if (peerApplicationData.hasRemaining()) {
            peerApplicationData.flip()
            return peerApplicationData.copyBytesTo(buffer)
        }
        peerPacketData.compact()

        val bytesRead: Int = channel.read(peerPacketData)
        /*
		 * If bytesRead are 0 put we still have some data in peerPacketData still to an unwrap (for testcase 1.1.6)
		 */
        /*
		 * If bytesRead are 0 put we still have some data in peerPacketData still to an unwrap (for testcase 1.1.6)
		 */
        if (bytesRead > 0 || peerPacketData.hasRemaining()) {
            peerPacketData.flip()
            while (peerPacketData.hasRemaining()) {
                peerApplicationData.compact()
                val result: SSLEngineResult
                try {
                    result = engine.unwrap(peerPacketData, peerApplicationData)
                } catch (e: SSLException) {
                    throw e
                }
                return when (result.status) {
                    OK -> {
                        peerApplicationData.flip()
                        peerApplicationData.copyBytesTo(buffer)
                    }
                    BUFFER_UNDERFLOW -> {
                        peerApplicationData.flip()
                        peerApplicationData.copyBytesTo(buffer)
                    }
                    BUFFER_OVERFLOW -> {
                        peerApplicationData = enlargeApplicationBuffer(peerApplicationData)
                        read(buffer)
                    }
                    CLOSED -> {
                        engine.closeOutbound()
                        try {
                            doHandshake()
                        } catch (e: IOException) {
                            //Just ignore this exception since we are closing the connection already
                        }
                        channel.close()
                        buffer.clear()
                        -1
                    }
                    else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                }
            }
        } else if (bytesRead < 0) {
            try {
                engine.closeInbound()
            } catch (e: Exception) {}
            engine.closeOutbound()
            try {
                doHandshake()
            } catch (e: IOException) {
                //Just ignore this exception since we are closing the connection already
            }
            channel.close()
        }
        peerApplicationData.copyBytesTo(buffer)
        return bytesRead


//        if (!buffer.hasRemaining()) {
//            return 0
//        }
//
//        if( peerApplicationData.hasRemaining() ) {
//            peerApplicationData.flip()
//            return peerApplicationData.copyBytesTo(buffer)
//        }
//        peerPacketData.compact();
//
//        val bytesRead: Int = channel.read(peerPacketData)
//
//        if (bytesRead > 0 || peerPacketData.hasRemaining()) {
//            peerPacketData.flip()
//            while (peerPacketData.hasRemaining()) {
//                peerApplicationData.compact()
//                val result: SSLEngineResult = engine.unwrap(peerPacketData, peerApplicationData)
//
//                return when (result.status) {
//                    OK -> {
//                        peerApplicationData.flip()
//                        peerApplicationData.copyBytesTo(buffer)
//                    }
//                    BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while reading.")
//                    BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while reading.")
//                    CLOSED -> {
//                        close()
//                        buffer.clear()
//                        -1
//                    }
//                    else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while reading channel.")
//                }
//            }
//        } else if (bytesRead < 0) {
//            engine.closeInbound()
//            close()
//        }
//
//        return peerApplicationData.copyBytesTo(buffer)
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

    @Throws(IOException::class)
    fun performHandshake(): Boolean {
        return doHandshake()
        /*
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

        return true
         */
    }

    /**
     * Event called when handshake requires data to be wrapped.
     */
    @Throws(SSLException::class)
    private fun onNeedWrap(): HandshakeStatus {
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
                return result.handshakeStatus
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

    /**
     * Implements the handshake protocol between two peers, required for the establishment of the SSL/TLS connection.
     * During the handshake, encryption configuration information - such as the list of available cipher suites - will be exchanged
     * and if the handshake is successful will lead to an established SSL/TLS session.
     *
     *
     *
     * A typical handshake will usually contain the following steps:
     *
     *
     *  * 1. wrap:     ClientHello
     *  * 2. unwrap:   ServerHello/Cert/ServerHelloDone
     *  * 3. wrap:     ClientKeyExchange
     *  * 4. wrap:     ChangeCipherSpec
     *  * 5. wrap:     Finished
     *  * 6. unwrap:   ChangeCipherSpec
     *  * 7. unwrap:   Finished
     *
     *
     *
     * Handshake is also used during the end of the session, in order to properly close the connection between the two peers.
     * A proper connection close will typically include the one peer sending a CLOSE message to another, and then wait for
     * the other's CLOSE message to close the transport link. The other peer from his perspective would read a CLOSE message
     * from his peer and then enter the handshake procedure to send his own CLOSE message as well.
     *
     * @param channel - the socket channel that connects the two peers.
     * @param engine - the engine that will be used for encryption/decryption of the data exchanged with the other peer.
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException - if an error occurs during read/write to the socket channel.
     */
    @Throws(IOException::class)
    private fun doHandshake(): Boolean {
        var result: SSLEngineResult
        var handshakeStatus: HandshakeStatus

        // NioSslPeer's fields applicationData and peerApplicationData are supposed to be large enough to hold all message data the peer
        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
        // to be used for the handshake, while keeping client's buffers at the same size.
        val appBufferSize = engine.session.applicationBufferSize
        applicationData = ByteBuffer.allocate(appBufferSize)
        peerApplicationData = ByteBuffer.allocate(appBufferSize)
        packetData.clear()
        peerPacketData.clear()
        handshakeStatus = engine.handshakeStatus
        var handshakeComplete = false
        loop@ while (!handshakeComplete) {
            when (handshakeStatus) {
                FINISHED -> {
                    handshakeComplete = !this.peerPacketData.hasRemaining()
                    if (handshakeComplete) return true
                    channel.write(this.peerPacketData)
                }
                NEED_UNWRAP -> {
                    if (channel.read(peerPacketData) < 0) {
                        if (engine.isInboundDone && engine.isOutboundDone) {
                            return false
                        }
                        try {
                            engine.closeInbound()
                        } catch (e: SSLException) {
                            //Ignore, cant do anything against this exception
                        }
                        engine.closeOutbound()
                        // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
                        handshakeStatus = engine.handshakeStatus
                        break@loop
                    }
                    peerPacketData.flip()
                    try {
                        result = engine.unwrap(peerPacketData, peerApplicationData)
                        peerPacketData.compact()
                        handshakeStatus = result.handshakeStatus
                    } catch (sslException: SSLException) {
                        engine.closeOutbound()
                        handshakeStatus = engine.handshakeStatus
                        break@loop
                    }
                    when (result.status) {
                        OK -> {
                        }
                        BUFFER_OVERFLOW ->                            // Will occur when peerApplicationData's capacity is smaller than the data derived from peerPacketData's unwrap.
                            peerApplicationData = enlargeApplicationBuffer(peerApplicationData)
                        BUFFER_UNDERFLOW ->                            // Will occur either when no data was read from the peer or when the peerPacketData buffer was too small to hold all peer's data.
                            peerPacketData = handleBufferUnderflow(peerPacketData)
                        CLOSED -> return if (engine.isOutboundDone) {
                            false
                        } else {
                            engine.closeOutbound()
                            handshakeStatus = engine.handshakeStatus
                            break@loop
                        }
                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                    }
                }
                NEED_WRAP -> {
                    packetData.clear()
                    try {
                        result = engine.wrap(applicationData, packetData)
                        handshakeStatus = result.handshakeStatus
                    } catch (sslException: SSLException) {
                        engine.closeOutbound()
                        handshakeStatus = engine.handshakeStatus
                        break@loop
                    }
                    when (result.status) {
                        OK -> {
                            packetData.flip()
                            while (packetData.hasRemaining()) {
                                channel.write(packetData)
                            }
                        }
                        BUFFER_OVERFLOW ->                            // Will occur if there is not enough space in packetData buffer to write all the data that would be generated by the method wrap.
                            // Since packetData is set to session's packet size we should not get to this point because SSLEngine is supposed
                            // to produce messages smaller or equal to that, but a general handling would be the following:
                            packetData = enlargePacketBuffer(packetData)
                        BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.")
                        CLOSED -> try {
                            packetData.flip()
                            while (packetData.hasRemaining()) {
                                channel.write(packetData)
                            }
                            // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerPacketData is clear to read.
                            peerPacketData.clear()
                        } catch (e: Exception) {
                            handshakeStatus = engine.handshakeStatus
                        }
                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                    }
                }
                NEED_TASK -> {
                    var task: Runnable?
                    while (engine.delegatedTask.also { task = it } != null) {
                        task?.run()
                    }
                    handshakeStatus = engine.handshakeStatus
                }
                NOT_HANDSHAKING -> {
                }
                else -> throw IllegalStateException("Invalid SSL status: $handshakeStatus")
            }
        }
        return true
    }

//    protected open fun doHandshake(channel: channel, engine: SSLEngine): Boolean {
//        var result: SSLEngineResult
//        var handshakeStatus: HandshakeStatus
//
//        // NioSslPeer's fields applicationData and peerApplicationData are supposed to be large enough to hold all message data the peer
//        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
//        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
//        // to be used for the handshake, while keeping client's buffers at the same size.
//        val appBufferSize = engine.session.applicationBufferSize
//        val applicationData = ByteBuffer.allocate(appBufferSize)
//        var peerApplicationData = ByteBuffer.allocate(appBufferSize)
//        packetData.clear()
//        peerPacketData.clear()
//        handshakeStatus = engine.handshakeStatus
//        loop@ while (handshakeStatus != FINISHED && handshakeStatus != NOT_HANDSHAKING) {
//            when (handshakeStatus) {
//                NEED_UNWRAP -> {
//                    if (channel.read(peerPacketData) < 0) {
//                        if (engine.isInboundDone && engine.isOutboundDone) {
//                            return false
//                        }
//                        try {
//                            engine.closeInbound()
//                        } catch (e: SSLException) {}
//                        engine.closeOutbound()
//                        // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
//                        handshakeStatus = engine.handshakeStatus
//                        continue@loop
//                    }
//                    peerPacketData.flip()
//                    try {
//                        result = engine.unwrap(peerPacketData, peerApplicationData)
//                        peerPacketData.compact()
//                        handshakeStatus = result.handshakeStatus
//                    } catch (sslException: SSLException) {
//                        engine.closeOutbound()
//                        handshakeStatus = engine.handshakeStatus
//                        continue@loop
//                    }
//                    when (result.status) {
//                        OK -> {
//                        }
//                        BUFFER_OVERFLOW ->                     // Will occur when peerApplicationData's capacity is smaller than the data derived from peerPacketData's unwrap.
//                            peerApplicationData = enlargeApplicationBuffer(engine, peerApplicationData)
//                        BUFFER_UNDERFLOW ->                     // Will occur either when no data was read from the peer or when the peerPacketData buffer was too small to hold all peer's data.
//                            peerPacketData = handleBufferUnderflow(engine, peerPacketData)
//                        CLOSED -> return if (engine.isOutboundDone) {
//                            false
//                        } else {
//                            engine.closeOutbound()
//                            handshakeStatus = engine.handshakeStatus
//                            continue@loop
//                        }
//                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
//                    }
//                }
//                NEED_WRAP -> {
//                    packetData.clear()
//                    try {
//                        result = engine.wrap(applicationData, packetData)
//                        handshakeStatus = result.handshakeStatus
//                    } catch (sslException: SSLException) {
//                        engine.closeOutbound()
//                        handshakeStatus = engine.handshakeStatus
//                        continue@loop
//                    }
//                    when (result.status) {
//                        OK -> {
//                            packetData.flip()
//                            while (packetData.hasRemaining()) {
//                                channel.write(packetData)
//                            }
//                        }
//                        BUFFER_OVERFLOW ->                     // Will occur if there is not enough space in packetData buffer to write all the data that would be generated by the method wrap.
//                            // Since packetData is set to session's packet size we should not get to this point because SSLEngine is supposed
//                            // to produce messages smaller or equal to that, but a general handling would be the following:
//                            packetData = enlargePacketBuffer(engine, packetData)
//                        BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.")
//                        CLOSED -> try {
//                            packetData.flip()
//                            while (packetData.hasRemaining()) {
//                                channel.write(packetData)
//                            }
//                            // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerPacketData is clear to read.
//                            peerPacketData.clear()
//                        } catch (e: Exception) {
//                            handshakeStatus = engine.handshakeStatus
//                        }
//                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
//                    }
//                }
//                NEED_TASK -> {
//                    var task: Runnable?
//                    while (engine.delegatedTask.also { task = it } != null) {
//                        task?.run()
//                    }
//                    handshakeStatus = engine.handshakeStatus
//                }
//                else -> throw IllegalStateException("Invalid SSL status: $handshakeStatus")
//            }
//        }
//        return true
//    }

    private fun handleBufferUnderflow(buffer: ByteBuffer): ByteBuffer {
        return if (engine.session.packetBufferSize < buffer.limit()) {
            buffer
        } else {
            val replaceBuffer = enlargePacketBuffer(buffer)
            buffer.flip()
            replaceBuffer.put(buffer)
            replaceBuffer
        }
    }

    private fun enlargePacketBuffer(buffer: ByteBuffer): ByteBuffer {
        return enlargeBuffer(buffer, engine.session.packetBufferSize)
    }

    /**
     * Enlarging a packet buffer (peerApplicationData or myAppData)
     *
     * @param buffer the buffer to enlarge
     * @return the enlarged buffer
     */
    private fun enlargeApplicationBuffer(buffer: ByteBuffer): ByteBuffer {
        return enlargeBuffer(buffer, engine.session.applicationBufferSize)
    }

    protected open fun enlargePacketBuffer(engine: SSLEngine, buffer: ByteBuffer): ByteBuffer {
        return enlargeBuffer(buffer, engine.session.packetBufferSize)
    }

    protected open fun enlargeApplicationBuffer(engine: SSLEngine, buffer: ByteBuffer): ByteBuffer {
        return enlargeBuffer(buffer, engine.session.applicationBufferSize)
    }

    /**
     * Compares `sessionProposedCapacity` with buffer's capacity. If buffer's capacity is smaller,
     * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
     * with capacity twice the size of the initial one.
     *
     * @param buffer - the buffer to be enlarged.
     * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by [SSLSession].
     * @return A new buffer with a larger capacity.
    `` */
    protected open fun enlargeBuffer(buffer: ByteBuffer, sessionProposedCapacity: Int): ByteBuffer {
        var buffer = buffer
        buffer = if (sessionProposedCapacity > buffer.capacity()) {
            ByteBuffer.allocate(sessionProposedCapacity)
        } else {
            ByteBuffer.allocate(buffer.capacity() * 2)
        }
        return buffer
    }

    /**
     * Handles [SSLEngineResult.Status.BUFFER_UNDERFLOW]. Will check if the buffer is already filled, and if there is no space problem
     * will return the same buffer, so the client tries to read again. If the buffer is already filled will try to enlarge the buffer either to
     * session's proposed size or to a larger capacity. A buffer underflow can happen only after an unwrap, so the buffer will always be a
     * peerPacketData buffer.
     *
     * @param buffer - will always be peerPacketData buffer.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     * @throws Exception
     */
    protected open fun handleBufferUnderflow(engine: SSLEngine, buffer: ByteBuffer): ByteBuffer {
        return if (engine.session.packetBufferSize < buffer.limit()) {
            buffer
        } else {
            val replaceBuffer = enlargePacketBuffer(engine, buffer)
            buffer.flip()
            replaceBuffer.put(buffer)
            replaceBuffer
        }
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