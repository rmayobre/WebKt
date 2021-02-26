package engine

import app.NetworkApplication
import channel.register
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import operation.Operation
import operation.OperationsChannel
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.CancellationException
import kotlin.jvm.Throws

interface NetworkChannelEngine  {

    /**
     * Is the engine running?
     */
    val isRunning: Boolean

    /**
     * Start your engine.
     */
    @Throws(IOException::class)
    fun NetworkApplication.start(/* Create some kind of interface to communicate with. */): OperationsChannel


    /**
     * Stop the engine.
     * @param timeout How long to wait, in milliseconds, for the NetworkChannelEngine to stop.
     * @param cause A throwable to send to all running operations within the NetworkChannelEngine.
     * @throws IOException thrown if engine had trouble shutting down it's IO operations or closing it's IO objects.
     */
    @Throws(IOException::class)
    suspend fun NetworkApplication.stop(
        timeout: Long? = null,
        cause: CancellationException? = null
    )
}

/**
 * Creates an [actor] that registers incoming [Operation] to the [Selector].
 * @param selector A selector provided that the channels will register to.
 */
@ObsoleteCoroutinesApi
@Throws(IOException::class)
@Suppress("BlockingMethodInNonBlockingContext")
internal fun NetworkApplication.operationsActor(selector: Selector): SendChannel<Operation> =
        appScope.actor {
            for (operation in channel) {
                with(operation) {
                    when (this) {
                        is Operation.Accept ->  channel.register(selector, SelectionKey.OP_ACCEPT, attachment)
                        is Operation.Connect -> channel.register(selector, SelectionKey.OP_CONNECT, attachment)
                        is Operation.Read ->    channel.register(selector, SelectionKey.OP_READ, attachment)
                        is Operation.Write ->   channel.register(selector, SelectionKey.OP_WRITE, attachment)
                    }
                }
            }
        }

