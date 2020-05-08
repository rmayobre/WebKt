import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class ServerSocketChannelEngine(
    private val executor: ExecutorService,
    private val address: InetSocketAddress
) : Thread(ENGINE_THREAD) {

    private lateinit var serverSocketChannel: ServerSocketChannel

    private lateinit var selector: Selector

    var isRunning: Boolean = false
        private set

    override fun start() {
        isRunning = true
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open().apply {
            bind(address)
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
        super.start()
    }

    override fun run() {
        while (isRunning) {
            if (selector.select() > 0) {
                val selectedKeys: Set<SelectionKey> = selector.selectedKeys()
                selectedKeys.forEach { key ->
                    if (key.isValid) {
                        try {
                            when {
                                key.isAcceptable -> {
                                    val channel: SocketChannel = serverSocketChannel.accept()
                                    executor.submit {
                                        if (onAccept(channel)) {
                                            channel.register(selector, CHANNEL_OPS)
                                        } else {
                                            channel.close()
                                        }
                                    }
                                }

                                key.isReadable -> executor.submit { onRead(key) }

                                // TODO Handle write event
//                            key.isWritable -> {}
                            }
                        } catch (ex: IOException) {
                            key.cancel()
                        } catch (ex: Exception) {
                            // Any exceptions not handled result in
                            // a channel shutdown and key removal.
                            key.channel().close()
                            key.cancel()
                        }
                    }
                }
            }
        }
    }

    fun stop(
        timeout: Long = TERMINATION_TIMEOUT_SECONDS,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ) {
        isRunning = false
        try {
            executor.shutdown()
            executor.awaitTermination(timeout, timeUnit)
        } catch (ex: InterruptedException) {
            executor.shutdownNow()
        }
        selector.close()
        serverSocketChannel.close()
    }

    /**
     * Should this channel be accepted? It is encourage at this time to configure the channel before accepting.
     * @return Return true if the engine registers the channel to the selector; false will close the channel
     */
    @Throws(IOException::class)
    abstract fun onAccept(channel: SocketChannel): Boolean

    /**
     * SelectionKey has a channel with available data to read.
     */
    @Throws(IOException::class, TimeoutException::class)
    abstract fun onRead(key: SelectionKey)

    companion object {
        /** The executor will wait 60 seconds for it's tasks to finish before termination. */
        private const val ENGINE_THREAD = "Channel-Engine-Thread"
        /** Ops for the accepted channels. */
        private const val CHANNEL_OPS = SelectionKey.OP_READ or SelectionKey.OP_WRITE
        private const val TERMINATION_TIMEOUT_SECONDS = 60L
    }
}