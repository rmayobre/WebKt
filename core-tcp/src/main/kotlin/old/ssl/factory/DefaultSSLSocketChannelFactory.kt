package old.ssl.factory

import old.ssl.SSLSocketChannel
import old.ssl.SSLSocketChannelImpl
import java.nio.channels.SocketChannel
import java.util.concurrent.Executor
import javax.net.ssl.SSLContext

class DefaultSSLSocketChannelFactory(
    private val context: SSLContext,
    private val clientMode: Boolean,
    private val executor: Executor? = null
): SSLSocketChannelFactory {

    override fun create(channel: SocketChannel): SSLSocketChannel = SSLSocketChannelImpl(
        channel = channel,
        engine = context.createSSLEngine().apply {
            useClientMode = clientMode
            needClientAuth = !clientMode
        },
        executor = executor
    )
}