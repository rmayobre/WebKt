package tcp.ssl.factory

import tcp.ssl.SSLSocketChannel
import tcp.ssl.SSLSocketChannelImpl
import java.nio.channels.SocketChannel
import java.util.concurrent.Executor
import javax.net.ssl.SSLContext

class DefaultSSLSocketChannelFactory(
    private val context: SSLContext,
    private val clientMode: Boolean,
    private val executor: Executor?
): SSLSocketChannelFactory {

    constructor(context: SSLContext, clientMode: Boolean): this(context, clientMode, null)

    constructor(context: SSLContext, executor: Executor): this(context, true, executor)

    constructor(context: SSLContext): this(context, true, null)

    override fun create(channel: SocketChannel): SSLSocketChannel = SSLSocketChannelImpl(
        channel = channel,
        engine = context.createSSLEngine().apply {
            useClientMode = clientMode
            needClientAuth = !clientMode
        }
    )
}