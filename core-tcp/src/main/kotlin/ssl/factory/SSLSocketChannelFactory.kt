package ssl.factory

import ssl.SSLSocketChannel
import java.nio.channels.SocketChannel

interface SSLSocketChannelFactory {
    fun create(channel: SocketChannel): SSLSocketChannel
}