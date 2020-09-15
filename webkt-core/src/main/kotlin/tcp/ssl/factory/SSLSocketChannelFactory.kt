package tcp.ssl.factory

import tcp.ssl.SSLSocketChannel
import java.nio.channels.SocketChannel

interface SSLSocketChannelFactory {
    fun create(channel: SocketChannel): SSLSocketChannel
}