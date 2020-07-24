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
    private val name: String = DEFAULT_THREAD_NAME
): ServerEngine, Runnable {

    private lateinit var serverChannel: ServerSocketChannel

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    private var _isRunning: Boolean = false

    override val isRunning: Boolean
        get() = if (::thread.isInitialized) {
            thread.isAlive && _isRunning
        } else {
            false
        }

    override fun start() {
        _isRunning = true
        selector = Selector.open()
        serverChannel = ServerSocketChannel.open()
        thread = Thread(this, name)
        thread.start()
    }

    override fun run() {
        // After all variables have been created, configure server settings.
        onCreate(serverChannel, selector)

        // Process the selector and accept/read sockets while
        // the thread is alive and stop hasn't been called.
        while (_isRunning) {
            if (selector.selectNow() > 0) {
                val selectedKeys: MutableSet<SelectionKey> = selector.selectedKeys()
                val iterator: MutableIterator<SelectionKey> = selectedKeys.iterator()
                while(iterator.hasNext()) {
                    val key: SelectionKey = iterator.next()
                    iterator.remove()
                    try {
                        if (key.isValid) {
                            when {
                                key.isAcceptable -> onAccept(key)
                                key.isReadable -> onRead(key)
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
    protected fun stop(timeout: Long, timeUnit: TimeUnit) {
        _isRunning = false
        try {
            service.shutdown()
            service.awaitTermination(timeout, timeUnit)
        } catch (ex: InterruptedException) {
            service.shutdownNow()
        }
        selector.close()
        serverChannel.close()
    }


    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun register(channel: SocketChannel, operation: Int = SelectionKey.OP_READ) =
        register(channel, null, operation)

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun register(channel: SocketChannel, attachment: Any?, operation: Int = SelectionKey.OP_READ) {
        if (channel.isOpen) {
            channel.register(selector, operation, attachment)
        }
    }

    /**
     * Unregister channel from selector.
     */
    protected fun unregister(channel: SocketChannel) {
        channel.keyFor(selector).let { key: SelectionKey ->
            key.cancel()
        }
    }

    /**
     * Called after the engine has created it's thread, channel, and selector. At this point,
     * it is recommended to configure your channel and register the selector. This step is
     * optional and not required to function. Only override if you need specific configurations.
     * @param channel the engine's ServerSocketChannel.
     * @param selector the Selector created for the ServerSocketChannel.
     */
    @Throws(IOException::class)
    protected open fun onCreate(channel: ServerSocketChannel, selector: Selector) {
        with(channel) {
            bind(address)
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
    }

    @Throws(IOException::class)
    protected open fun onAccept(key: SelectionKey) {
        serverChannel.accept()?.let { channel: SocketChannel ->
            service.execute {
                try {
                    onAccept(channel.apply {
                        configureBlocking(false)
                    })
                } catch (ex: Exception) {
                    onException(ex)
                }
            }
        }
    }

    @Throws(IOException::class)
    protected open fun onRead(key: SelectionKey) {
        service.execute {
            try {
                onRead(
                    channel = key.channel() as SocketChannel,
                    attachment = key.attachment()
                )
            } catch (ex: Exception) {
                onException(ex)
            }
        }
        key.cancel()
    }

    /**
     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
     * the channel to the appropriate settings during this function call.
     * @throws IOException when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    protected abstract fun onAccept(channel: SocketChannel)

    /**
     * A channel is ready to be read by the engine's implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    protected abstract fun onRead(channel: SocketChannel, attachment: Any?)

    /**
     * Reports an exception occurred while processing a channel.
     */
    protected abstract fun onException(ex: Exception)

    companion object {
        private const val DEFAULT_THREAD_NAME = "server-socket-channel-engine"
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
}