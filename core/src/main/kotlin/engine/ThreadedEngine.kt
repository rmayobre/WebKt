package engine

import app.NetworkApplication
import channel.NetworkChannel
import engine.operation.Operation
import engine.operation.OperationsChannel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Runnable
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class ThreadedEngine<T : NetworkChannel<*>>(
    private val threadName: String = DEFAULT_THREAD_NAME
): NetworkChannelEngine {

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
                        is Operation.Accept ->
                            selector.register(message.channel, SelectionKey.OP_ACCEPT, message.attachment)
                        is Operation.Connect ->
                            selector.register(message.channel, SelectionKey.OP_CONNECT, message.attachment)
                        is Operation.Read ->
                            selector.register(message.channel, SelectionKey.OP_READ, message.attachment)
                        is Operation.Write ->
                            selector.register(message.channel, SelectionKey.OP_WRITE, message.attachment)
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
    }
}