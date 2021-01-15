package engine

import kotlinx.coroutines.*
import operation.Operation
import java.lang.Runnable
import java.nio.channels.*

abstract class AbstractChannelEngine(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val threadName: String = DEFAULT_THREAD_NAME
): ChannelEngine, Runnable {

    private val supervisor = SupervisorJob()

    protected val coroutineScope: CoroutineScope =
        CoroutineScope(dispatcher + supervisor)

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    override val isRunning: Boolean
        get() = if (::thread.isInitialized && ::selector.isInitialized) {
            thread.isAlive && selector.isOpen
        } else {
            false
        }

    override fun start() {
        selector = Selector.open()
        thread = Thread(this, threadName)
        thread.start()
    }

    override fun run() {
        while (selector.isOpen) {
            synchronized(selector) {
                if (selector.selectNow() > 0) {
                    selector.selectedKeys().forEach { key ->
                        if (key.isValid) {
                            when {
                                // A channel has an incoming connection request.
                                key.isAcceptable -> coroutineScope.launch { onAccept(key) }

                                // A channel has able to connect to remote address.
                                key.isConnectable -> coroutineScope.launch { onConnect(key) }

                                // A channel, registered to the selector, has incoming data.
                                key.isReadable -> coroutineScope.launch { onRead(key) }

                                // A channel, registered to the selector, has data to be written.
                                key.isWritable -> coroutineScope.launch { onWrite(key) }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun stop() {
        if (isRunning) {
            selector.close()
            // TODO finish jobs while selector is closed
//            supervisor.completeExceptionally()
        }
    }

    override fun register(channel: SelectableChannel, operation: Operation, attachment: Any?) {
        if (channel.isOpen) {
            channel.register(selector, operation.flag, attachment)
        }
    }

    /**
     * An operations event of the engine.AbstractChannelEngine. This means the Selector has provided a SelectionKey that is able to
     * accept an incoming connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open suspend fun onAccept(key: SelectionKey) {
        key.cancel()
    }

    /**
     * An operations event of the engine.AbstractChannelEngine. This means the Selector has provided a SelectionKey that has
     * a channel, ready to finish connection.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open suspend fun onConnect(key: SelectionKey) {
        key.cancel()
    }

    /**
     * An operations event of the engine.AbstractChannelEngine. This means the Selector has provided a SelectionKey with a channel
     * that has incoming data being sent from the opposing endpoint.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open suspend fun onRead(key: SelectionKey) {
        key.cancel()
    }

    /**
     * An operations event of the engine.AbstractChannelEngine. This means the Selector has provided a SelectionKey that is ready
     * for it's channel to write data.
     * @param key The SelectionKey providing the SelectableChannel with a new incoming connection.
     */
    protected open suspend fun onWrite(key: SelectionKey) {
        key.cancel()
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "engine.AbstractChannelEngine-thread"
    }
}