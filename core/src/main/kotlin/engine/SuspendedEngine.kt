package engine

import app.NetworkApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import operation.Operation
import operation.OperationsChannel
import java.io.IOException
import java.nio.channels.Selector
import kotlin.jvm.Throws

class SuspendedEngine(
    private val coroutineName: String = DEFAULT_COROUTINE_NAME
) : NetworkChannelEngine {

    private lateinit var job: Job

    private lateinit var selector: Selector

    private lateinit var channel: SendChannel<Operation>

    override val isRunning: Boolean
        get() = ::job.isInitialized &&
            ::selector.isInitialized &&
            ::channel.isInitialized &&
            job.isActive &&
            selector.isOpen

    @ObsoleteCoroutinesApi
    override fun NetworkApplication.start(): OperationsChannel {
        if (!isRunning) {
            selector = Selector.open()
            job = launchSelectorJob()
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
            timeout?.let {
                withContext(appScope.coroutineContext) {
                    withTimeoutOrNull(timeout) {
                        job.join()
                    } ?: cause?.let {
                        job.cancel(it)
                    } ?: job.cancel()
                }
            } ?: job.cancelAndJoin()
        }
    }

    @Throws(IOException::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun NetworkApplication.launchSelectorJob(): Job = appScope.launch(
        context = CoroutineName(coroutineName)
    ) {
        while(selector.isOpen) {
            launch {
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
            }.join()
        }
    }

    companion object {
        private const val DEFAULT_COROUTINE_NAME = "SuspendedEngine-coroutine"
    }
}