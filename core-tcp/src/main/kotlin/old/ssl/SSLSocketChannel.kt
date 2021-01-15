package old.ssl

import tls.HandshakeResult
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import java.util.concurrent.Executor
import javax.net.ssl.*
import kotlin.jvm.Throws

/**
 * A Secure Socket Layer implementation of a ByteChannel. This channel is not selectable. Registering this
 * channel to a Selector will not work, however, it can be an attachment. This feature may become available
 * in future iterations. Essentially, this channel is a Wrapper for a SocketChannel and a SSEngine with the
 * added function [performHandshake].
 */
interface SSLSocketChannel : ByteChannel, Channel {

    /** Get the native SelectableChannel this SSLSocketChannel runs off. */
    val channel: SelectableChannel

    /** Channel's SSLSession created from SSLEngine. */
    val session: SSLSession

    /** Get the socket's InetAddress */
    val inetAddress: InetAddress

    /** Get the channel's remote address. */
    val remoteAddress: SocketAddress

    /** Get the channel's remote port. */
    val remotePort: Int

    /** Get the channel's local address. */
    val localAddress: SocketAddress

    /** Get the channel's local port. */
    val localPort: Int

    /**
     * Bind a local address for the channel.
     * @return current instance of channel.
     */
    fun bind(address: SocketAddress): SSLSocketChannel

    /**
     * Implements the handshake protocol between two peers, required for the establishment of the SSL/TLS connection.
     * During the handshake, encryption configuration information - such as the list of available cipher suites - will be exchanged
     * and if the handshake is successful will lead to an established SSL/TLS session.
     *
     * Handshake is also used during the end of the session, in order to properly close the connection between the two peers.
     * A proper connection close will typically include the one peer sending a CLOSE message to another, and then wait for
     * the other's CLOSE message to close the transport link. The other peer from his perspective would read a CLOSE message
     * from his peer and then enter the handshake procedure to send his own CLOSE message as well.
     *
     * Example handshake process:
     *
     * 1. wrap:     ClientHello
     * 2. unwrap:   ServerHello/Cert/ServerHelloDone
     *
     *    unwrap (continued):
     *              The unwrap process could happen multiple
     *              times if the SocketChannel is non-blocking.
     *
     * 3. wrap:     ClientKeyExchange
     * 4. wrap:     ChangeCipherSpec
     * 5. wrap:     Finished
     * 6. unwrap:   ChangeCipherSpec
     * 7. unwrap:   Finished
     *
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException if an error occurs during read/write to the socket channel.
     */
    @Throws(IOException::class)
    suspend fun performHandshake(): HandshakeResult

    companion object {

        /**
         * Open a SSLSocketChannel with basic client configurations.
         * @param address the socket address for the SSLSocketChannel to connect to.
         * @param context Security context for the connection.
         * @param executor An optional executor to handle delegated tasks in the background.
         */
        @Deprecated("remove")
        fun client(address: SocketAddress, context: SSLContext, executor: Executor? = null): SSLSocketChannel =
            open(
                address = address,
                engine = context.createSSLEngine().apply {
                    useClientMode = true
                    needClientAuth = false
                },
                executor = executor
            )

        /**
         * Open a SSLSocketChannel with basic server configurations.
         * @param address the socket address for the SSLSocketChannel to connect to.
         * @param context Security context for the connection.
         * @param executor An optional executor to handle delegated tasks in the background.
         */
        @Deprecated("remove")
        fun server(address: SocketAddress, context: SSLContext, executor: Executor? = null): SSLSocketChannel =
            open(
                address = address,
                engine = context.createSSLEngine().apply {
                    useClientMode = false
                    needClientAuth = true
                },
                executor = executor
            )

        /**
         * Opens an SSLSocketChannel with the provided SocketAddress and SSLEngine.
         * @param address the socket address for the SSLSocketChannel to connect to.
         * @param context Security context for the connection.
         * @param executor An optional executor to handle delegated tasks in the background.
         */
        @Deprecated("remove")
        fun open(address: SocketAddress, engine: SSLEngine, executor: Executor? = null): SSLSocketChannel =
            SSLSocketChannelImpl(
                channel = SocketChannel.open(address),
                engine = engine,
                executor = executor
            )
    }
}

