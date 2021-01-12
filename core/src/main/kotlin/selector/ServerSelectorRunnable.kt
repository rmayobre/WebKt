package selector

import kotlinx.coroutines.*
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

class ServerSelectorRunnable(
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
        private val handler: ServerSelectorHandler,
        selector: Selector
) : SelectorRunnable(selector) {

    private val job = SupervisorJob()

    private val scope: CoroutineScope = CoroutineScope(dispatcher + job)

    override fun onAccept(key: SelectionKey) {
        try {
//            val serverChannel = key.channel() as ServerSocketChannel
//            serverChannel.accept()?.let { channel: SocketChannel ->
//                service.execute {
//                    try {
//                        channel.configureBlocking(false)
//                        handler.onChannelAccepted(channel)
//                    } catch (ex: Exception) {
//                        handler.onException(key.channel(), key.attachment(), ex)
//                    }
//                }
//            }
            val channel = key.channel()
            scope.launch {
                handler.onChannelAccepted(channel)
            }
        } catch (ex: SecurityException) {
            // SecurityManager has prevented acceptance to the remote endpoint.
            // Keep the run function async by submitting this exception to
            // the ExecutorServer.
            scope.launch {
                handler.onException(key.channel(), key.attachment(), ex)
            }
            key.cancel()
//            service.execute {
//                handler.onException(key.channel(), key.attachment(), ex)
//            }
//            key.cancel()
        } catch (ex: Exception) {
            scope.launch {
                handler.onException(key.channel(), key.attachment(), ex)
            }
            key.cancel()
        }
    }

    override fun onRead(key: SelectionKey) {
        service.execute {
            try {
                handler.onReadChannel(
                        channel = key.channel() as SocketChannel,
                        attachment = key.attachment()
                )
            } catch (ex: Exception) {
                handler.onException(key.channel(), key.attachment(), ex)
            }
        }
        key.cancel()
    }

    override fun onWrite(key: SelectionKey) {
        service.execute {
            try {
                handler.onWriteChannel(
                        channel = key.channel() as SocketChannel,
                        attachment = key.attachment()
                )
            } catch (ex: Exception) {
                handler.onException(key.channel(), key.attachment(), ex)
            }
        }
        key.cancel()
    }
}