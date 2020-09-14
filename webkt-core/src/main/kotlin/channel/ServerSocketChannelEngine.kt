package channel

import channel.selector.ACCEPT_OPERATION
import channel.selector.READ_OPERATION
import channel.selector.ServerSelectorHandler
import channel.selector.WRITE_OPERATION
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.*
import java.util.concurrent.ExecutorService

/**
 * An implementation of the AbstractServerSocketChannelEngine. Note that this implementation provides
 * no secure layer to the network connections.
 * @param address The InetSocketAddress for the ServerSocketChannel to bind to.
 * @param service The ExecutorServer to launch multi-threaded calls for each Channel operation.
 * @param threadName Name of the Thread that runs the Selector operation calls.
 */
abstract class ServerSocketChannelEngine(
    address: InetSocketAddress,
    service: ExecutorService,
    threadName: String = DEFAULT_THREAD_NAME
) : AbstractServerSocketChannelEngine(address, service, threadName), ServerSelectorHandler {

    override val handler: ServerSelectorHandler
        get() = this

    /**
     * Register channel into Selector as accept operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see register
     */
    @Throws(IOException::class)
    protected fun registerToAccept(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, ACCEPT_OPERATION, attachment)

    /**
     * Register channel into Selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see register
     */
    @Throws(IOException::class)
    protected fun registerToRead(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, READ_OPERATION, attachment)

    /**
     * Register channel into Selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see register
     */
    @Throws(IOException::class)
    protected fun registerToWrite(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, WRITE_OPERATION, attachment)

    companion object {
        private const val DEFAULT_THREAD_NAME = "server-socket-channel-engine"
    }
}