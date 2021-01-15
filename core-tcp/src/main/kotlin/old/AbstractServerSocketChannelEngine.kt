package old

import operation.*
import operation.handler.ServerOperationsHandler
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * An abstract implementation of non-blocking server operations. This implementation makes use of java's
 * [Selector] and [SelectableChannel]s by providing a lifecycle influenced event series. Events called
 * are based from the operations declared by the Selector created at runtime.
 * @param address The InetSocketAddress for the ServerSocketChannel to bind to.
 * @param service The ExecutorServer to launch multi-threaded calls for each Channel operation.
 * @param threadName Name of the Thread that runs the Selector operation calls.
 * @see ServerSelectorRunnable
 * @see ServerOperationsHandler
 */
abstract class AbstractServerSocketChannelEngine(
    private val address: InetSocketAddress,
    private val service: ExecutorService,
    private val threadName: String
) : ServerEngine {

    private lateinit var selectorRunnable: ServerSelectorRunnable

    private lateinit var serverChannel: ServerSocketChannel

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    protected abstract val handler: ServerOperationsHandler

    override val isRunning: Boolean
        get() = if (::thread.isInitialized) {
            thread.isAlive
        } else {
            false
        }

    override fun start() {
        selector = Selector.open()
        selectorRunnable = ServerSelectorRunnable(service, handler, selector)
        serverChannel = ServerSocketChannel.open().also { onConfigure(it) }
        thread = Thread(selectorRunnable, threadName)
        thread.start()
    }

    override fun stop() = stop(
        timeout = DEFAULT_TIMEOUT_SECONDS,
        timeUnit = TimeUnit.SECONDS
    )

    /**
     * Stop the ServerSocketChannel and all running tasks. Function can be overridden, however, you must call the super
     * function in order to actually stop the engine from processing threads.
     *
     * ## Shutdown order on call
     * 1. Selector is closed - this is to prevent any further operations.
     * 2. ExecutorService performs shutdown - allow pending operations a certain amount of time to finish.
     * 3. Loop through Selector's keys - Close channel associated with key, then cancel key.
     *
     * @param timeout How long the engine will wait for pending processes to finish before a forced shutdown.
     * @param timeUnit The unit of time the engine will use to measure the timeout length.
     */
    @Throws(IOException::class)
    protected open fun stop(timeout: Long, timeUnit: TimeUnit) {
        selector.close()
        try {
            service.shutdown()
            service.awaitTermination(timeout, timeUnit)
        } catch (ex: InterruptedException) {
            service.shutdownNow()
        }
        selector.keys().forEach { key ->
            key.channel().close()
            key.cancel()
        }
    }

    /**
     * Called after the engine has created it's thread, channel, and selector. At this point,
     * it is recommended to configure your channel and register the selector. This step is
     * optional and not required to function. Only override if you need specific configurations.
     *
     * @param channel the engine's ServerSocketChannel.
     */
    @Throws(IOException::class)
    protected open fun onConfigure(channel: ServerSocketChannel) {
        with(channel) {
            bind(address)
            configureBlocking(false)
        }
        register(channel, SelectionKey.OP_ACCEPT)
    }

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     * @see AcceptOperation
     * @see CONNECT_OPERATION
     * @see READ_OPERATION
     * @see WRITE_OPERATION
     */
    @Throws(IOException::class)
    protected fun register(channel: SelectableChannel, operation: Int, attachment: Any? = null) =
        selectorRunnable.register(channel, operation, attachment)

    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
 }