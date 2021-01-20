package engine

import NetworkApplication
import engine.operation.OperationMessage
import engine.operation.OperationsChannel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Runnable
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class ThreadedEngine(
    private val threadName: String = DEFAULT_THREAD_NAME
): NetworkChannelEngine {

    private lateinit var operationsActor: SendChannel<OperationMessage>

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    override val isRunning: Boolean
        get() = ::thread.isInitialized &&
                ::selector.isInitialized &&
                thread.isAlive &&
                selector.isOpen

    @ObsoleteCoroutinesApi
    override fun NetworkApplication.start(): OperationsChannel {
        if (!isRunning) {
            selector = Selector.open()
            thread = Thread(threadedEngineRunnable(), threadName)
            thread.start()
        }

        // It might be better to launch a coroutine instead of the actor.
        operationsActor = appScope.actor {
            for (message in channel) {
                when(message) {
                    is OperationMessage.Accept ->
                        selector.register(message.channel, SelectionKey.OP_ACCEPT, message.attachment)
                    is OperationMessage.Connect ->
                        selector.register(message.channel, SelectionKey.OP_CONNECT, message.attachment)
                    is OperationMessage.Read ->
                        selector.register(message.channel, SelectionKey.OP_READ, message.attachment)
                    is OperationMessage.Write ->
                        selector.register(message.channel, SelectionKey.OP_WRITE, message.attachment)
                }
            }
        }
    }

    private fun NetworkApplication.threadedEngineRunnable() = Runnable {
        while (selector.isOpen) {
            synchronized(selector) {
                if (selector.selectNow() > 0) {
                    selector.selectedKeys().forEach { key ->
                        if (key.isValid) {
                            onValidKey(key)
                        }
                    }
                }
            }
        }
    }

    override fun stop() {
        if (isRunning) {
            selector.close()
            selector.keys().forEach { key ->
                key.channel().close()
            }
            operationsActor.close() // TODO custom throwable for channel to signify a normal close.
//            thread.join() TODO wait for thread to finish
        }
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "NetworkChannelEngine-thread"

        /**
         * Register channel back into selector. Only registers channel if channel is open.
         * @param channel SelectableChannel to be registered to Selector.
         * @param operation Operation the Channel will be registered to perform. NOTE, you can register multiple operations at the same time.
         * @param attachment An attachment to be provided for the channel's next engine.operation.
         */
        private fun Selector.register(channel: SelectableChannel, operation: Int, attachment: Any?) {
            if (channel.isOpen && this.isOpen) {
                channel.register(this, operation, attachment)
            }
        }
    }
}