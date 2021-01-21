package engine.attachment

import channel.NetworkChannel
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectableChannel
import java.nio.channels.SocketChannel

sealed class Attachment<T : SelectableChannel>(
    open val channel: NetworkChannel<T>,
    open val storage: Any? = null
)

data class SocketChannelAttachment<T : NetworkChannel<SocketChannel>>(
    override val channel: T,
    override val storage: Any? = null
) : Attachment<SocketChannel>(channel, storage)

data class DatagramChannelAttachment<T : NetworkChannel<DatagramChannel>>(
    override val channel: T,
    override val storage: Any? = null
) : Attachment<DatagramChannel>(channel, storage)

inline fun <reified T : NetworkChannel<*>> Attachment<*>.toTypeOf(
    block: (channel: T, storage: Any?) -> Unit
) {
    when(this) {
        is SocketChannelAttachment<*> -> {
            if (channel !is T) {
                block(channel as T, storage)
            }
        }
        is DatagramChannelAttachment<*> -> {
            if (channel !is T) {
                block(channel as T, storage)
            }
        }
    }
}