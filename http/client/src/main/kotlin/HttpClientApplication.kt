import app.SocketChannelApplication
import channel.tcp.SuspendedSocketChannel
import engine.NetworkChannelEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import message.Message
import message.Response
import java.net.SocketAddress
import java.nio.channels.SelectionKey

abstract class HttpClientApplication(
    override val engine: NetworkChannelEngine,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SocketChannelApplication(dispatcher){

    final override suspend fun onConnect(channel: SuspendedSocketChannel, attachment: Any?) {
        TODO("Not yet implemented")
    }

    final override suspend fun onRead(channel: SuspendedSocketChannel, attachment: Any?) {
        TODO("Not yet implemented")
    }

    final override suspend fun onWrite(channel: SuspendedSocketChannel, attachment: Any?) {
        TODO("Not yet implemented")
    }

    final override suspend fun onException(key: SelectionKey, cause: Throwable) {
        TODO("Not yet implemented")
    }

    final override suspend fun onException(channel: SuspendedSocketChannel, attachment: Any?, error: Throwable) {
        TODO("Not yet implemented")
    }
}
