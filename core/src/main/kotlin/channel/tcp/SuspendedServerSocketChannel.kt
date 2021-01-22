package channel.tcp

import channel.NetworkChannel
import channel.tls.SecureSocketChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.*
import javax.net.ssl.SSLEngine
import kotlin.jvm.Throws

class SuspendedServerSocketChannel(
    override val channel: ServerSocketChannel,
    private val engine: SSLEngine? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NetworkChannel<ServerSocketChannel>, Channel by channel {

    override val inetAddress: InetAddress
        get() = channel.socket().inetAddress

    override val localAddress: SocketAddress
        get() = channel.localAddress

    override val localPort: Int
        get() = channel.socket().localPort

    /**
     * Constructs a SuspendedSocketChannel with a standard SocketChannel. The SocketChannel is not
     * connected to a remote address yet.
     * @throws IOException An I/O related error was thrown
     */
    @Throws(IOException::class)
    constructor(): this(ServerSocketChannel.open())


    /**
     * Constructs a SuspendedSocketChannel with a standard SocketChannel. The SocketChannel is not
     * connected to a remote address yet.
     * @throws IOException An I/O related error was thrown
     */
    @Throws(IOException::class)
    constructor(engine: SSLEngine):
            this(ServerSocketChannel.open(), engine)

    /**
     * Constructs a SuspendedSocketChannel with a standard SocketChannel. The SocketChannel is not
     * connected to a remote address yet.
     * @throws IOException An I/O related error was thrown
     */
    @Throws(IOException::class)
    constructor(engine: SSLEngine, dispatcher: CoroutineDispatcher):
            this(ServerSocketChannel.open(), engine, dispatcher)


    init {
        if (channel.isBlocking) {
            channel.configureBlocking(false)
        }
    }

    override fun bind(local: SocketAddress) {
        channel.bind(local)
    }

    fun accept(): AcceptResult = try {
        AcceptResult.Successful(
            channel = if (engine != null) {
                SecureSocketChannel(channel.accept()!!, engine, dispatcher)
            } else {
                SuspendedSocketChannel(channel.accept()!!)
            }
        )
    } catch (ex: Exception) {
        AcceptResult.Failure(ex)
    }

    override fun toString(): String =
        "SuspendedServerSocketChannel: ${hashCode()}\n" +
                "Channel Class:     ${channel.javaClass}\n" +
                "Local Address:     $localAddress\n" +
                "Local Port:        $localPort\n"
}