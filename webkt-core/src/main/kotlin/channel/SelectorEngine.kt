package channel

import ServerEngine
import java.io.IOException
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

abstract class SelectorEngine(
    private val name: String = DEFAULT_THREAD_NAME
) : ServerEngine, Runnable {

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
        thread = Thread(this, name)
        thread.start()
    }

    override fun run() {
        while (_isRunning) {
            if (selector.selectNow() > 0) {
                selector.selectedKeys().forEach { key ->
                    try {
                        if (key.isValid) {
                            when {
                                // A channel has an incoming connection request.
                                key.isAcceptable -> onAccept(key)

                                // A channel has connected to
                                key.isConnectable -> onConnect(key)

                                // A channel, registered to the selector, has incoming data.
                                key.isReadable -> onRead(key)

                                // A channel, registered to the selector, has data to be written.
                                key.isWritable -> onWrite(key)
                            }
                        }
                    } catch (ex: Exception) {
                        onException(ex, key)
                    }
                }
            }
        }
    }

    override fun stop() {
        _isRunning = false
        selector.close()
    }

    /**
     * Register channel back into selector as accept operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun registerToAccept(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, attachment, SelectionKey.OP_ACCEPT)

    /**
     * Register channel back into selector as connect operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun registerToConnect(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, attachment, SelectionKey.OP_CONNECT)

    /**
     * Register channel back into selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun registerToWrite(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, attachment, SelectionKey.OP_WRITE)


    /**
     * Register channel back into selector as a read operation. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    protected fun registerToRead(channel: SelectableChannel, attachment: Any? = null) =
        register(channel, attachment, SelectionKey.OP_READ)

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @throws IOException if channel cannot be registered to selector.
     */
    @Throws(IOException::class)
    private fun register(channel: SelectableChannel, attachment: Any?, operation: Int) {
        if (channel.isOpen) {
            channel.register(selector, operation, attachment)
        }
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that is able to
     * accept an incoming connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     * @throws IOException Usually thrown when the SelectableChannel cannot accept the new connection.
     */
    @Throws(IOException::class)
    protected open fun onAccept(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that has
     * a channel, ready to finish connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     * @throws IOException Usually thrown when the SelectableChannel cannot accept the new connection.
     */
    @Throws(IOException::class)
    protected open fun onConnect(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey with a channel
     * that has incoming data being sent from the opposing endpoint.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     * @throws IOException Usually thrown when the SelectableChannel cannot accept the new connection.
     */
    @Throws(IOException::class)
    protected open fun onRead(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that is ready
     * for it's channel to write data.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     * @throws IOException Usually thrown when the SelectableChannel cannot accept the new connection.
     */
    @Throws(IOException::class)
    protected open fun onWrite(key: SelectionKey) {
        key.cancel()
    }

    /**
     * Reports an exception occurred while processing a channel.
     * @param ex Exception that was thrown.
     * @param key Key being used while exception was thrown.
     */
    protected abstract fun onException(ex: Exception, key: SelectionKey)

    companion object {
        private const val DEFAULT_THREAD_NAME = "selector-engine"
    }
}