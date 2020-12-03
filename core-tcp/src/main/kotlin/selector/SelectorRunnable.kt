package selector

import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

/**
 * Register a SelectableChannel to accept incoming connection requests.
 * @see SelectionKey.OP_ACCEPT
 */
const val ACCEPT_OPERATION = SelectionKey.OP_ACCEPT

/**
 * Register a SelectableChannel to connect to a remote connection.
 * @see SelectionKey.OP_CONNECT
 */
const val CONNECT_OPERATION = SelectionKey.OP_CONNECT

/**
 * Register a SelectableChannel to read incoming data.
 * @see SelectionKey.OP_READ
 */
const val READ_OPERATION = SelectionKey.OP_READ

/**
 * Register a SelectableChannel to write data to endpoint.
 * @see SelectionKey.OP_WRITE
 */
const val WRITE_OPERATION = SelectionKey.OP_WRITE

/**
 * # Summary
 * A Runnable implementation for a non-blocking Selector setup. Make use of this class by
 * creating a child class that inherits the SelectorRunnable. Override the functions called
 * during operation events.
 *
 * ## Running the task
 * Create a Thread and pass the runnable within the constructor. Then call [Thread.start] to
 * run the Runnable within the Thread.
 *
 * ```
 * val selector = Selector.open()
 * val runnable = SelectorRunnable(selector)
 * val thread = Thread(runnable)
 *
 * // Start function.
 * thread.start()
 * ```
 *
 * ## Registering channels
 * Register a channel with an operation by calling the [register] function.
 * ```
 * val channel = ServerSocketChannel.open()
 * val runnable = SelectorRunnable(selector)
 *
 * // Register a ServerSocketChannel to accept incoming connections
 * runnable.register(channel, ACCEPT_OPERATION)
 * ```
 *
 * ## Overriding operation events
 * By default, operation events result with the SelectionKey being canceled. To change this,
 * create an implementation of SelectorRunnable to override operation events.
 *
 * ```
 * val selector = Selector.open()
 * val runnable = object : SelectorRunnable(selector) {
 *
 *      override fun onAccept(key: SelectionKey) {
 *          // Accept incoming connections from the channel within the provided key.
 *      }
 *
 *      override fun onConnect(key: SelectionKey) {
 *          // Finish connection to the channel within the key.
 *      }
 *
 *      override fun onRead(key: SelectionKey) {
 *          // Read from the channel within the key.
 *      }
 *
 *      override fun onWrite(key: SelectionKey) {
 *          // Write data to the channel within the key.
 *      }
 * }
 * ```
 *
 * ## How to stop run method
 * To stop the running task within your Thread, keep a reference of the Selector passed to
 * the SelectorRunnable and close it using the [Selector.close] method.
 */
open class SelectorRunnable(
    private val selector: Selector
) : Runnable {

    override fun run() {
        while (selector.isOpen) {
            synchronized(selector) {
                if (selector.selectNow() > 0) {
                    selector.selectedKeys().forEach { key ->
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
                    }
                }
            }
        }
    }

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @param channel SelectableChannel to be registered to Selector.
     * @param operation Operation the Channel will be registered to perform. NOTE, you can register multiple operations at the same time.
     * @param attachment An attachment to be provided for the channel's next operation.
     * @see ACCEPT_OPERATION
     * @see CONNECT_OPERATION
     * @see READ_OPERATION
     * @see WRITE_OPERATION
     */
    fun register(channel: SelectableChannel, operation: Int, attachment: Any? = null) {
        if (channel.isOpen) {
            channel.register(selector, operation, attachment)
        }
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that is able to
     * accept an incoming connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open fun onAccept(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that has
     * a channel, ready to finish connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open fun onConnect(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey with a channel
     * that has incoming data being sent from the opposing endpoint.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open fun onRead(key: SelectionKey) {
        key.cancel()
    }

    /**
     * A lifecycle event of the SelectorEngine. This means the Selector has provided a SelectionKey that is ready
     * for it's channel to write data.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open fun onWrite(key: SelectionKey) {
        key.cancel()
    }

//    /**
//     * Reports an exception occurred while processing a channel.
//     * @param ex Exception that was thrown.
//     * @param key Key being used while exception was thrown.
//     */
//    protected open fun onException(ex: Exception, key: SelectionKey) {
//        key.cancel()
//    }
}