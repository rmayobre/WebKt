package channel

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class ServerSocketChannelEngine(
    /** Network address the server engine binds to. */
    val address: InetSocketAddress,
    private val service: ExecutorService,
    name: String = DEFAULT_THREAD_NAME
): SelectorEngine(name), Runnable {

    private lateinit var serverChannel: ServerSocketChannel

    override fun start() {
        super.start()
        serverChannel = ServerSocketChannel.open()
        onConfigure(serverChannel)
    }

    override fun stop() = stop(
        timeout = DEFAULT_TIMEOUT_SECONDS,
        timeUnit = TimeUnit.SECONDS
    )

    /**
     * Stop the ServerSocketChannel and all running tasks. Function can be overridden, however, you must call the super
     * function in order to actually stop the engine from processing threads.
     * @param timeout How long the engine will wait for pending processes to finish before a forced shutdown.
     * @param timeUnit The unit of time the engine will use to measure the timeout length.
     */
    protected open fun stop(timeout: Long, timeUnit: TimeUnit) {
        super.stop()
        try {
            service.shutdown()
            service.awaitTermination(timeout, timeUnit)
        } catch (ex: InterruptedException) {
            service.shutdownNow()
        }
        serverChannel.close()
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
        registerToAccept(channel)
    }

    @Throws(IOException::class)
    override fun onAccept(key: SelectionKey) {
        serverChannel.accept()?.let { channel: SocketChannel ->
            service.execute {
                try {
                    channel.configureBlocking(false)
                    onChannelAccepted(channel)
                } catch (ex: Exception) {
                    onException(ex, key)
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun onRead(key: SelectionKey) {
        service.execute {
            try {
                onReadChannel(
                    channel = key.channel() as SocketChannel,
                    attachment = key.attachment()
                )
            } catch (ex: Exception) {
                onException(ex, key)
            }
        }
        key.cancel()
    }

    @Throws(IOException::class)
    override fun onWrite(key: SelectionKey) {
        service.execute {
            try {
                onWriteChannel(
                    channel = key.channel() as SocketChannel,
                    attachment = key.attachment()
                )
            } catch (ex: Exception) {
                onException(ex, key)
            }
        }
        key.cancel()
    }

    override fun onException(ex: Exception, key: SelectionKey) {
        key.cancel()
        onException(key.channel() as SocketChannel, key.attachment(), ex)
    }

    /**
     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
     * the channel to the appropriate settings during this function call.
     * @throws IOException when a channel cannot be accept or read from.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    protected abstract fun onChannelAccepted(channel: SocketChannel)

    /**
     * A channel is ready to be read by the engine's child implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    protected abstract fun onReadChannel(channel: SocketChannel, attachment: Any?)

    /**
     * A channel is ready to be written by the engine's child implementation.
     * @throws IOException when a channel cannot be read from.
     * @throws TimeoutException when a channel is taking too long to read from.
     */
    @Throws(IOException::class, TimeoutException::class)
    protected abstract fun onWriteChannel(channel: SocketChannel, attachment: Any?)

    /**
     * Reports an exception occurred while processing a channel. All channels are removed
     * from Selector after an exception occurs, by default.
     * @param ex Exception that was thrown.
     * @param channel The channel being used while the exception occurred.
     * @param attachment a nullable attachment provided with SocketChannel
     */
    protected abstract fun onException(channel: SocketChannel, attachment: Any?, ex: Exception)

    companion object {
        private const val DEFAULT_THREAD_NAME = "server-socket-channel-engine"
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
}

//abstract class ServerSocketChannelEngine(
//    /** Network address the server engine binds to. */
//    val address: InetSocketAddress,
//    private val service: ExecutorService,
//    private val name: String = DEFAULT_THREAD_NAME
//): ServerEngine, Runnable {
//
//    private lateinit var serverChannel: ServerSocketChannel
//
//    private lateinit var selector: Selector
//
//    private lateinit var thread: Thread
//
//    private var _isRunning: Boolean = false
//
//    override val isRunning: Boolean
//        get() = if (::thread.isInitialized) {
//            thread.isAlive && _isRunning
//        } else {
//            false
//        }
//
//    override fun start() {
//        _isRunning = true
//        selector = Selector.open()
//        serverChannel = ServerSocketChannel.open()
//        thread = Thread(this, name)
//        thread.start()
//    }
//
//    override fun run() {
//        // After all variables have been created, configure server settings.
//        onConfigure(serverChannel, selector)
//
//        // Process the selector and accept/read sockets while
//        // the thread is alive and stop hasn't been called.
//        while (_isRunning) {
////            println("Number of channels that changed: ${selector.select()}")
//            if (selector.selectNow() > 0) {
////                val selectedKeys: MutableSet<SelectionKey> = selector.selectedKeys()
////                val iterator: MutableIterator<SelectionKey> = selectedKeys.iterator()
////                while(iterator.hasNext()) {
////                    val key: SelectionKey = iterator.next()
////                    iterator.remove()
//                selector.selectedKeys().forEach { key ->
//                    try {
//                        if (key.isValid) {
//                            when {
//                                // ServerSocketChannel has an incoming connection request.
//                                key.isAcceptable -> onAccept(key)
//
//                                // A channel, registered to the selector, has incoming data.
//                                key.isReadable -> onRead(key)
//
//                                // A channel, registered to the selector, has data to be written.
//                                key.isWritable -> onWrite(key)
//                            }
//                        }
//                    } catch (ex: Exception) {
//                        onException(ex, key)
//                    }
//                }
//            }
//        }
//    }
//
//    override fun stop() = stop(
//        timeout = DEFAULT_TIMEOUT_SECONDS,
//        timeUnit = TimeUnit.SECONDS
//    )
//
//    /**
//     * Stop the ServerSocketChannel and all running tasks. Function can be overridden, however, you must call the super
//     * function in order to actually stop the engine from processing threads.
//     * @param timeout How long the engine will wait for pending processes to finish before a forced shutdown.
//     * @param timeUnit The unit of time the engine will use to measure the timeout length.
//     */
//    protected open fun stop(timeout: Long, timeUnit: TimeUnit) {
//        _isRunning = false
//        try {
//            service.shutdown()
//            service.awaitTermination(timeout, timeUnit)
//        } catch (ex: InterruptedException) {
//            service.shutdownNow()
//        }
//        selector.close()
//        serverChannel.close()
//    }
//
//    /**
//     * Register channel back into selector as a read operation. Only registers channel if channel is open.
//     * @throws IOException if channel cannot be registered to selector.
//     */
//    @Throws(IOException::class)
//    protected fun registerToWrite(channel: SelectableChannel, attachment: Any? = null) =
//            register(channel, attachment, SelectionKey.OP_WRITE)
//
//
//    /**
//     * Register channel back into selector as a read operation. Only registers channel if channel is open.
//     * @throws IOException if channel cannot be registered to selector.
//     */
//    @Throws(IOException::class)
//    protected fun registerToRead(channel: SelectableChannel, attachment: Any? = null) =
//            register(channel, attachment, SelectionKey.OP_READ)
//
//    /**
//     * Register channel back into selector. Only registers channel if channel is open.
//     * @throws IOException if channel cannot be registered to selector.
//     */
//    @Throws(IOException::class)
//    private fun register(channel: SelectableChannel, attachment: Any?, operation: Int) {
//        if (channel.isOpen) {
//            channel.register(selector, operation, attachment)
//        }
//    }
//
//    /**
//     * Called after the engine has created it's thread, channel, and selector. At this point,
//     * it is recommended to configure your channel and register the selector. This step is
//     * optional and not required to function. Only override if you need specific configurations.
//     * @param channel the engine's ServerSocketChannel.
//     * @param selector the Selector created for the ServerSocketChannel.
//     */
//    @Throws(IOException::class)
//    protected open fun onConfigure(channel: ServerSocketChannel, selector: Selector) {
//        with(channel) {
//            bind(address)
//            configureBlocking(false)
//            register(selector, SelectionKey.OP_ACCEPT)
//        }
//    }
//
//    /**
//     *
//     */
//    @Throws(IOException::class)
//    protected open fun onAccept(key: SelectionKey) {
//        serverChannel.accept()?.let { channel: SocketChannel ->
//            service.execute {
//                try {
//                    channel.configureBlocking(false)
//                    onChannelAccepted(channel)
//                } catch (ex: Exception) {
//                    onException(ex, key)
//                }
//            }
//        }
//    }
//
//    /**
//     *
//     */
//    @Throws(IOException::class)
//    protected open fun onRead(key: SelectionKey) {
//        service.execute {
//            try {
//                onReadChannel(
//                    channel = key.channel() as SocketChannel,
//                    attachment = key.attachment()
//                )
//            } catch (ex: Exception) {
//                onException(ex, key)
//            }
//        }
//        key.cancel()
//    }
//
//    /**
//     *
//     */
//    @Throws(IOException::class)
//    protected open fun onWrite(key: SelectionKey) {
//        service.execute {
//            try {
//                onWriteChannel(
//                        channel = key.channel() as SocketChannel,
//                        attachment = key.attachment()
//                )
//            } catch (ex: Exception) {
//                onException(ex, key)
//            }
//        }
//        key.cancel()
//    }
//
//    /**
//     * A new connection was made, as well as a new channel was constructed. Should this channel's connection
//     * be accepted and allow the channel to be read from? If you accept the channel, it is recommended to configure
//     * the channel to the appropriate settings during this function call.
//     * @throws IOException when a channel cannot be accept or read from.
//     * @return Return true if the engine registers the channel to the selector; false will close the channel
//     */
//    @Throws(IOException::class)
//    protected abstract fun onChannelAccepted(channel: SocketChannel)
//
//    /**
//     * A channel is ready to be read by the engine's child implementation.
//     * @throws IOException when a channel cannot be read from.
//     * @throws TimeoutException when a channel is taking too long to read from.
//     */
//    @Throws(IOException::class, TimeoutException::class)
//    protected abstract fun onReadChannel(channel: SocketChannel, attachment: Any?)
//
//    /**
//     * A channel is ready to be written by the engine's child implementation.
//     * @throws IOException when a channel cannot be read from.
//     * @throws TimeoutException when a channel is taking too long to read from.
//     */
//    @Throws(IOException::class, TimeoutException::class)
//    protected abstract fun onWriteChannel(channel: SocketChannel, attachment: Any?)
//
//    /**
//     * Reports an exception occurred while processing a channel.
//     * @param ex Exception that was thrown.
//     * @param key Key being used while exception was thrown.
//     */
//    protected abstract fun onException(ex: Exception, key: SelectionKey)
//
//    companion object {
//        private const val DEFAULT_THREAD_NAME = "server-socket-channel-engine"
//        private const val DEFAULT_TIMEOUT_SECONDS = 60L
//    }
//}