import message.Message
import message.Response
import java.net.SocketAddress

interface HttpClient {

    suspend fun connect(remote: SocketAddress): Boolean

    suspend fun send(message: Message): Response
}