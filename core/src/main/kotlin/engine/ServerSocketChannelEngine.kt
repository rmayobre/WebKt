package engine

import kotlinx.coroutines.*
import operation.handler.ServerOperationsHandler
import java.nio.channels.*


class ServerSocketChannelEngine(
    private val handler: ServerOperationsHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    threadName: String = DEFAULT_THREAD_NAME
) : AbstractChannelEngine(dispatcher, threadName) {

    constructor(handler: ServerOperationsHandler, threadName: String):
        this(handler, Dispatchers.IO, threadName)

    override suspend fun onAccept(key: SelectionKey): Unit = with(coroutineScope) {
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
            handler.onChannelAccepted(
                channel = key.channel(),
                attachment = key.attachment()
            )
        }
    }

    override suspend fun onRead(key: SelectionKey): Unit = with(coroutineScope) {
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

    override suspend fun onWrite(key: SelectionKey): Unit = with(coroutineScope) {
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
        private const val DEFAULT_THREAD_NAME = "engine.ServerChannelEngine-thread"
    }
}