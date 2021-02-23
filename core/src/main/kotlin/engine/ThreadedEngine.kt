package engine

import app.NetworkApplication
import channel.SuspendedNetworkChannel
import operation.Operation
import operation.OperationsChannel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Runnable
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class ThreadedEngine<T : SuspendedNetworkChannel<*>>(
    private val threadName: String = DEFAULT_THREAD_NAME
): NetworkChannelEngine<T> {

    private lateinit var operationsActor: SendChannel<Operation<T>>

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    override val isRunning: Boolean
        get() = ::thread.isInitialized &&
                ::selector.isInitialized &&
                thread.isAlive &&
                selector.isOpen

    @ObsoleteCoroutinesApi
    override fun NetworkApplication.start(): OperationsChannel<T> {
        if (!isRunning) {
            selector = Selector.open()
            thread = Thread(threadedEngineRunnable(), threadName)
            thread.start()
            // It might be better to launch a coroutine instead of the actor.
            operationsActor = appScope.actor {
                for (message in channel) {
                    when (message) {
                        is Operation.Accept ->  selector.register(message.channel, SelectionKey.OP_ACCEPT, message.attachment)
                        is Operation.Connect -> selector.register(message.channel, SelectionKey.OP_CONNECT, message.attachment)
                        is Operation.Read ->    selector.register(message.channel, SelectionKey.OP_READ, message.attachment)
                        is Operation.Write ->   selector.register(message.channel, SelectionKey.OP_WRITE, message.attachment)
                    }
                }
            }
        }

        return OperationsChannel(operationsActor)
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

    override fun stop(timeout: Long?, cause: Throwable?) {
        if (isRunning) {
            selector.close()
            selector.keys().forEach { key ->
                key.channel().close()
            }
            operationsActor.close(cause)
            timeout?.let {
                thread.join(it)
            } ?: thread.join()
        }
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "NetworkChannelEngine-thread"
    }
}