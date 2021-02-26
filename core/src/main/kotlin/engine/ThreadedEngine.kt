package engine

import app.NetworkApplication
import kotlinx.coroutines.CancellationException
import operation.Operation
import operation.OperationsChannel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import java.lang.Runnable
import java.nio.channels.Selector

class ThreadedEngine(
    private val threadName: String = DEFAULT_THREAD_NAME
): NetworkChannelEngine {

    private lateinit var channel: SendChannel<Operation>

    private lateinit var selector: Selector

    private lateinit var thread: Thread

    override val isRunning: Boolean
        get() = ::thread.isInitialized &&
            ::selector.isInitialized &&
            ::channel.isInitialized &&
            thread.isAlive &&
            selector.isOpen

    @ObsoleteCoroutinesApi
    override fun NetworkApplication.start(): OperationsChannel {
        if (!isRunning) {
            selector = Selector.open()
            thread = Thread(threadedEngineRunnable(), threadName)
            thread.start()
            channel = operationsActor(selector)
        }
        return OperationsChannel(channel)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun NetworkApplication.stop(timeout: Long?, cause: CancellationException?) {
        if (isRunning) {
            selector.close()
            selector.keys().forEach { key ->
                key.channel().close()
            }
            channel.close(cause)
            try {
                timeout?.let {
                    thread.join(it)
                } ?: thread.join()
            } catch (ex: Exception) {

            }
        }
    }

    private fun NetworkApplication.threadedEngineRunnable() = Runnable {
        while (selector.isOpen) {
            synchronized(selector) {
                if (selector.selectNow() > 0) {
                    selector.selectedKeys().forEach { key ->
                        if (key.isValid) {
                            // Remove key from selector to prevent
                            // the key from being processed again.
                            key.cancel()
                            onValidKey(key)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "ThreadedEngine-thread"
    }
}