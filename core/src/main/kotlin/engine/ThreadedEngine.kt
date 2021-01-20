package engine

import engine.deprecated.Operation
import engine.operation.OperationMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.lang.Runnable
import java.nio.channels.SelectableChannel
import java.nio.channels.Selector

class ThreadedEngine(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val threadName: String = DEFAULT_THREAD_NAME
): NetworkChannelEngine, Runnable {

    private val job: Job = SupervisorJob()

    private val engineScope: CoroutineScope =
        CoroutineScope(dispatcher + job)

    private lateinit var selector: Selector

    private lateinit var channel: Channel<OperationMessage>

    private val thread: Thread by lazy {
        Thread(this, threadName)
    }

    override val isRunning: Boolean
        get() = if (::selector.isInitialized) {
            thread.isAlive && selector.isOpen
        } else {
            false
        }

    override fun start(vararg message: OperationMessage): SendChannel<OperationMessage> {
        if (!isRunning) {
            channel = Channel() // TODO research more into channels.
            selector = Selector.open()
            thread.start()
        }

    }

    // Should I change this to a coroutine job?
    override fun run() = with(engineScope) {
        while (selector.isOpen) {
            synchronized(selector) {
                if (selector.selectNow() > 0) {
                    selector.selectedKeys().forEach { key ->
                        if (key.isValid) {
                            when {
                                // A channel has an incoming connection request.
                                key.isAcceptable -> launch { onAccept(key) }

                                // A channel has able to connect to remote address.
                                key.isConnectable -> launch { onConnect(key) }

                                // A channel, registered to the selector, has incoming data.
                                key.isReadable -> launch { onRead(key) }

                                // A channel, registered to the selector, has data to be written.
                                key.isWritable -> launch { onWrite(key) }
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
            engineScope.cancel()
            selector.keys().forEach { key ->
                key.channel().close()
            }
//            thread.join() TODO wait for thread to finish
        }
    }

    private class ThreadedEngineRunnable(
        private val selector: Selector,
        scope: CoroutineScope
    ) : Runnable, CoroutineScope by scope {
        override fun run() {
            while (selector.isOpen) {
                synchronized(selector) {
                    if (selector.selectNow() > 0) {
                        selector.selectedKeys().forEach { key ->
                            if (key.isValid) {
                                when {
                                    // A channel has an incoming connection request.
                                    key.isAcceptable -> launch { onAccept(key) }

                                    // A channel has able to connect to remote address.
                                    key.isConnectable -> launch { onConnect(key) }

                                    // A channel, registered to the selector, has incoming data.
                                    key.isReadable -> launch { onRead(key) }

                                    // A channel, registered to the selector, has data to be written.
                                    key.isWritable -> launch { onWrite(key) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "NetworkChannelEngine-thread"
    }
}
/*

    /**
     * Register channel back into selector. Only registers channel if channel is open.
     * @param channel SelectableChannel to be registered to Selector.
     * @param operation Operation the Channel will be registered to perform. NOTE, you can register multiple operations at the same time.
     * @param attachment An attachment to be provided for the channel's next engine.operation.
     */
    @Deprecated("do something within the actor design.")
    private fun register(channel: SelectableChannel, operation: Operation, attachment: Any? = null) {
        if (channel.isOpen) {
            channel.register(selector, operation.flag, attachment)
        }
    }
 */