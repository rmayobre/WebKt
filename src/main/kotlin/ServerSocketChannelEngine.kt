import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

abstract class ServerSocketChannelEngine(
    address: InetSocketAddress,
    private val service: ExecutorService,
    private val name: String = DEFAULT_THREAD_NAME
): ServerEngine<SocketChannel>(address), Runnable {

    private lateinit var serverSocketChannel: ServerSocketChannel

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    private var running: Boolean = false

    val isRunning: Boolean
        get() = running && if (::thread.isInitialized) thread.isAlive else false

    override fun start() {
        running = true
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open().apply {
            bind(address)
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
        thread = Thread(this, name)
        thread.start()
    }

    override fun run() {
        while (running) {
            if (selector.selectNow() > 0) {
                val selectedKeys: MutableSet<SelectionKey> = selector.selectedKeys()
                val iterator: MutableIterator<SelectionKey> = selectedKeys.iterator()
                while(iterator.hasNext()) {
                    val key: SelectionKey = iterator.next()
                    iterator.remove()
                    try {
                        if (key.isValid) {
                            when {
                                key.isAcceptable -> {
                                    serverSocketChannel.accept()?.let { channel ->
                                        service.execute {
                                            try {
                                                if (onAccept(channel)) {
                                                    channel.register(selector, SelectionKey.OP_READ)
                                                } else {
                                                    channel.close()
                                                }
                                            } catch (ex: Exception) {
                                                onException(ex)
                                            }
                                        }
                                    }
                                }

                                key.isReadable -> {
                                    service.execute {
                                        try {
                                            onRead(key.channel() as SocketChannel)
                                        } catch (ex: Exception) {
                                            onException(ex)
                                        }
                                    }
                                    key.cancel()
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        onException(ex)
                    }
                }
            }
        }
    }

    override fun stop() = stop(
        timeout = DEFAULT_TIMEOUT_SECONDS,
        timeUnit = TimeUnit.SECONDS
    )

    /**
     * Stop the ServerSocketChannel and all running tasks.
     * @param timeout How long the engine will wait for pending processes to finish before a forced shutdown.
     * @param timeUnit The unit of time the engine will use to measure the timeout length.
     */
    fun stop(timeout: Long, timeUnit: TimeUnit) {
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
     * Register channel back into selector.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun register(channel: SocketChannel) {
        if (channel.isOpen) {
            channel.register(selector, SelectionKey.OP_READ)
        }
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "server-socket-channel-thread"
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
}