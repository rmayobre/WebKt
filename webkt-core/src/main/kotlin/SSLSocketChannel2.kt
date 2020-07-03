import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.NetworkChannel
import java.nio.channels.SocketChannel
import javax.management.remote.JMXConnectionNotification.CLOSED
import javax.net.ssl.*
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.SSLEngineResult.HandshakeStatus.*
import javax.net.ssl.SSLEngineResult.Status.*


class SSLSocketChannel2(
    private val channel: SocketChannel,
    private val engine: SSLEngine
) : ByteChannel, NetworkChannel by channel {

    /**
     * This side's un-encrypted data.
     */
    private val data: ByteBuffer

    /**
     * The decrypted data received from other endpoint.
     */
    private var decryptedPeerData: ByteBuffer

    /**
     * Encrypted data from this side.
     */
    private var encryptedData: ByteBuffer

    /**
     * The encrypted data received from other endpoint.
     */
    private var encryptedPeerData: ByteBuffer

    /**
     * Session created by SSLEngine.
     */
    private val session: SSLSession
        get() = engine.session

    /** Default client-side constructor. */
    constructor(host: String, port: Int) : this(SSLContext.getDefault(), host, port)

    /** Client-side constructor. */
    constructor(context: SSLContext, host: String, port: Int) : this(SocketChannel.open(), context.createSSLEngine(host, port)) {
        engine.useClientMode = true
    }

    init {
        // Initialize buffers for decrypted data.
        data = ByteBuffer.allocate(session.applicationBufferSize)
        decryptedPeerData = ByteBuffer.allocate(session.applicationBufferSize)
        // Initialize buffers for encrypted data. These will have direct
        // allocation because they will be used for IO operations.
        encryptedData = ByteBuffer.allocateDirect(session.packetBufferSize)
        encryptedPeerData = ByteBuffer.allocateDirect(session.packetBufferSize)
        // Start handshake with connection.
//        performHandshake()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        var bytesWritten = 0

        while (buffer.hasRemaining()) {
            encryptedData.clear()
            val result: SSLEngineResult = engine.wrap(buffer, encryptedData)
            when (result.status) {
                OK -> {
                    encryptedData.flip()
                    while (encryptedData.hasRemaining()) {
                        bytesWritten += channel.write(encryptedData)
                    }
                }
                BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while writing.")
                BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while writing.")
//                CLOSED -> {
//                    close()
//                    return bytesWritten
//                }
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


        val bytesRead: Int = channel.read(encryptedPeerData)

        if (bytesRead > 0 || encryptedPeerData.hasRemaining()) {
            while (encryptedPeerData.hasRemaining()) {
                decryptedPeerData.compact()
                val result: SSLEngineResult = engine.unwrap(encryptedPeerData, decryptedPeerData)

                return when (result.status) {
                    OK -> {
                        decryptedPeerData.flip()
                        decryptedPeerData.copyBytesTo(buffer)
                    }
                    BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occurred while reading.")
                    BUFFER_OVERFLOW -> throw SSLException("Buffer overflow occurred while reading.")
//                    CLOSED -> {
//                        close()
//                        buffer.clear()
//                        -1
//                    }
                    else -> throw IllegalStateException("SSLEngineResult status: ${result.status}, could not be determined while reading channel.")
                }
            }
        } else if (bytesRead < 0) {
            engine.closeInbound()
            close()
        }

        return decryptedPeerData.copyBytesTo(buffer)
    }

    @Throws(IOException::class)
    override fun close() {
        engine.closeOutbound()
//        performHandshake()
        channel.close()
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
     * @param socketChannel - the socket channel that connects the two peers.
     * @param engine - the engine that will be used for encryption/decryption of the data exchanged with the other peer.
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException - if an error occurs during read/write to the socket channel.
     */
//    @Throws(IOException::class)
//    protected fun doHandshake(socketChannel: SocketChannel, engine: SSLEngine): Boolean {
//        log.debug("About to do handshake...")
//        var result: SSLEngineResult
//        var handshakeStatus: HandshakeStatus
//
//        // NioSslPeer's fields myAppData and peerAppData are supposed to be large enough to hold all message data the peer
//        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
//        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
//        // to be used for the handshake, while keeping client's buffers at the same size.
//        val appBufferSize = engine.session.applicationBufferSize
//        val myAppData = ByteBuffer.allocate(appBufferSize)
//        var peerAppData = ByteBuffer.allocate(appBufferSize)
//        myNetData.clear()
//        peerNetData.clear()
//        handshakeStatus = engine.handshakeStatus
//        while (handshakeStatus != FINISHED && handshakeStatus != NOT_HANDSHAKING) {
//            when (handshakeStatus) {
//                NEED_UNWRAP -> {
//                    if (socketChannel.read(peerNetData) < 0) {
//                        if (engine.isInboundDone && engine.isOutboundDone) {
//                            return false
//                        }
//                        try {
//                            engine.closeInbound()
//                        } catch (e: SSLException) {
//                            log.error("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.")
//                        }
//                        engine.closeOutbound()
//                        // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
//                        handshakeStatus = engine.handshakeStatus
//                        break
//                    }
//                    peerNetData.flip()
//                    try {
//                        result = engine.unwrap(peerNetData, peerAppData)
//                        peerNetData.compact()
//                        handshakeStatus = result.handshakeStatus
//                    } catch (sslException: SSLException) {
//                        log.error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...")
//                        engine.closeOutbound()
//                        handshakeStatus = engine.handshakeStatus
//                        break
//                    }
//                    when (result.status) {
//                        OK -> {
//                        }
//                        BUFFER_OVERFLOW ->                     // Will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
//                            peerAppData = enlargeApplicationBuffer(engine, peerAppData)
//                        BUFFER_UNDERFLOW ->                     // Will occur either when no data was read from the peer or when the peerNetData buffer was too small to hold all peer's data.
//                            peerNetData = handleBufferUnderflow(engine, peerNetData)
//                        SSLEngineResult.Status.CLOSED -> return if (engine.isOutboundDone) {
//                            false
//                        } else {
//                            engine.closeOutbound()
//                            handshakeStatus = engine.handshakeStatus
//                            break
//                        }
//                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
//                    }
//                }
//                NEED_WRAP -> {
//                    myNetData.clear()
//                    try {
//                        result = engine.wrap(myAppData, myNetData)
//                        handshakeStatus = result.handshakeStatus
//                    } catch (sslException: SSLException) {
//                        log.error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...")
//                        engine.closeOutbound()
//                        handshakeStatus = engine.handshakeStatus
//                        break
//                    }
//                    when (result.status) {
//                        OK -> {
//                            myNetData.flip()
//                            while (myNetData.hasRemaining()) {
//                                socketChannel.write(myNetData)
//                            }
//                        }
//                        BUFFER_OVERFLOW ->                     // Will occur if there is not enough space in myNetData buffer to write all the data that would be generated by the method wrap.
//                            // Since myNetData is set to session's packet size we should not get to this point because SSLEngine is supposed
//                            // to produce messages smaller or equal to that, but a general handling would be the following:
//                            myNetData = enlargePacketBuffer(engine, myNetData)
//                        BUFFER_UNDERFLOW -> throw SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.")
//                        SSLEngineResult.Status.CLOSED -> try {
//                            myNetData.flip()
//                            while (myNetData.hasRemaining()) {
//                                socketChannel.write(myNetData)
//                            }
//                            // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerNetData is clear to read.
//                            peerNetData.clear()
//                        } catch (e: Exception) {
//                            println("Failed to send server's CLOSE message due to socket channel's failure.")
//                            handshakeStatus = engine.handshakeStatus
//                        }
//                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
//                    }
//                }
//                NEED_TASK -> {
//                    var task: Runnable?
//                    while (engine.delegatedTask.also { task = it } != null) {
//                        executor.execute(task)
//                    }
//                    handshakeStatus = engine.handshakeStatus
//                }
//                FINISHED -> {
//                }
//                NOT_HANDSHAKING -> {
//                }
//                else -> throw IllegalStateException("Invalid SSL status: $handshakeStatus")
//            }
//        }
//        return true
//    }

    @Throws(IOException::class)
    private fun wrap() {

    }

    @Throws(IOException::class)
    private fun unwrap() {

    }

    companion object {

        fun create(host: String, port: Int): SSLSocketChannel2? {
            return try {
                SSLSocketChannel2(host, port)
            } catch (ex: Exception) {
                null
            }
        }

        fun create(context: SSLContext, host: String, port: Int): SSLSocketChannel2? {
            return try {
                SSLSocketChannel2(context, host, port)
            } catch (ex: Exception) {
                null
            }
        }

        /**
         * Copy bytes from "this" ByteBuffer to the designated "buffer" ByteBuffer.
         * @param buffer The designated buffer for all bytes to move to.
         * @return number of bytes copied to the other buffer.
         */
        private fun ByteBuffer.copyBytesTo(buffer: ByteBuffer): Int = if (remaining() > buffer.remaining()) {
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