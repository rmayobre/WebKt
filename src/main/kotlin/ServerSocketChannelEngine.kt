import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class ServerSocketChannelEngine(
    override val address: InetSocketAddress,
    private val service: ExecutorService,
    private val name: String? = null
): ServerEngine, Runnable {

    private lateinit var serverSocketChannel: ServerSocketChannel

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    private var running: Boolean = false

    val isRunning: Boolean
        get() = running && thread.isAlive

    override fun start() {
        running = true
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open().apply {
            bind(address)
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
        thread = Thread(this, name ?: DEFAULT_THREAD_NAME)
        thread.start()
    }

    override fun run() {
        while (running) {
            if (selector.select() > 0) {
                val selectedKeys: Set<SelectionKey> = selector.selectedKeys()
                selectedKeys.forEach { key ->
                    if (key.isValid) {
                        try {
                            when {
                                key.isAcceptable -> {
                                    val channel: SocketChannel = serverSocketChannel.accept()
                                    service.execute {
                                        if (onAccept(channel)) {
                                            channel.register(selector, CHANNEL_OPS)
                                        } else {
                                            channel.close()
                                        }
                                    }
                                }

                                key.isReadable -> service.execute { onRead(key) }
                            }
                        } catch (ex: Exception) {
                            onException(key, ex)
                        }
                    }
                }
            }
        }
    }

    override fun stop(timeout: Long, timeUnit: TimeUnit) {
        running = false
        try {
            service.shutdown()
            service.awaitTermination(timeout, timeUnit)
        } catch (ex: InterruptedException) {
            service.shutdownNow()
        }
        selector.close()
        serverSocketChannel.close()
    }

    /**
     * Should this channel be accepted? It is encourage at this time to configure the channel before accepting.
     * This will run on it's own thread.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    protected abstract fun onAccept(channel: SocketChannel): Boolean

    /**
     * SelectionKey has a channel with available data to read. This will run on it's own thread.
     */
    @Throws(IOException::class, TimeoutException::class)
    protected abstract fun onRead(key: SelectionKey)

    protected abstract fun onException(key: SelectionKey, ex: Exception)

    companion object {
        private const val DEFAULT_THREAD_NAME = "server-socket-channel-thread"
        /** Ops for the accepted channels. */
        private const val CHANNEL_OPS = SelectionKey.OP_READ or SelectionKey.OP_WRITE
    }
}