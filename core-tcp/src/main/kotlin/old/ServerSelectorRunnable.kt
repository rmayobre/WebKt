package old

import engine.operation.handler.ServerOperationsHandler
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

class ServerSelectorRunnable(
    private val service: ExecutorService,
    private val handler: ServerOperationsHandler,
    selector: Selector
) : SelectorRunnable(selector) {

    override suspend fun onAccept(key: SelectionKey) {
        try {
            val serverChannel = key.channel() as ServerSocketChannel
            serverChannel.accept()?.let { channel: SocketChannel ->
                service.execute {
                    try {
                        channel.configureBlocking(false)
                        handler.onChannelAccepted(channel)
                    } catch (ex: Exception) {
                        handler.onException(key.channel(), key.attachment(), ex)
                    }
                }
            }
        } catch (ex: SecurityException) {
            // SecurityManager has prevented acceptance to the remote endpoint.
            // Keep the run function async by submitting this exception to
            // the ExecutorServer.
            service.execute {
                handler.onException(key.channel(), key.attachment(), ex)
            }
        } catch (ex: IOException) {
            // Something happened while accepting an incoming connection.
            key.cancel()
        }
    }

    override suspend fun onRead(key: SelectionKey) {
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

    override suspend fun onWrite(key: SelectionKey) {
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