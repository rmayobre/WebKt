package engine

import kotlinx.coroutines.*
import engine.operation.handler.ClientOperationsHandler
import java.nio.channels.SelectionKey

class SocketChannelEngine(
    private val handler: ClientOperationsHandler, // TODO make this an actor
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    threadName: String = DEFAULT_THREAD_NAME
) : AbstractChannelEngine(dispatcher, threadName) {

    constructor(handler: ClientOperationsHandler, threadName: String):
        this(handler, Dispatchers.IO, threadName)

    override suspend fun onConnect(key: SelectionKey): Unit = with(engineScope) {
        launch(
            CoroutineExceptionHandler { _, error ->
                launch {
                    handler.onException(
                        channel = key.channel(),
                        attachment = key.attachment(),
                        error)
                }
            }
        ) {
            handler.onConnect(key.channel())
        }
    }

    override suspend fun onRead(key: SelectionKey): Unit = with(engineScope) {
        launch(
            CoroutineExceptionHandler { _, error ->
                launch {
                    handler.onException(
                        channel = key.channel(),
                        attachment = key.attachment(),
                        error)
                }
            }
        ) {
            handler.onReadChannel(
                channel = key.channel(),
                attachment = key.attachment()
            )
        }
        key.cancel()
    }

    override suspend fun onWrite(key: SelectionKey): Unit = with(engineScope) {
        launch(
            CoroutineExceptionHandler { _, error ->
                launch {
                    handler.onException(
                        channel = key.channel(),
                        attachment = key.attachment(),
                        error)
                }
            }
        ) {
            handler.onWriteChannel(
                channel = key.channel(),
                attachment = key.attachment()
            )
        }
        key.cancel()
    }

    companion object {
        private const val DEFAULT_THREAD_NAME = "SocketChannelEngine-thread"
    }
}