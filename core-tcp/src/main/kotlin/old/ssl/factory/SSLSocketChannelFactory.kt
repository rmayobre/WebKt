package old.ssl.factory

import old.ssl.SSLSocketChannel
import java.nio.channels.SocketChannel

interface SSLSocketChannelFactory {
    fun create(channel: SocketChannel): SSLSocketChannel
}