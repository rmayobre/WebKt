package engine

import channel.NetworkChannel
import java.nio.channels.SelectableChannel

data class Attachment<T : SelectableChannel>(
    val channel: NetworkChannel<T>,
    val storage: Any? = null
)

inline fun <reified T : NetworkChannel<*>> Attachment<*>.toTypeOf(
    block: (channel: T, attachment: Any?) -> Unit
) {
    if (channel is T) {
        block(channel, storage)
    }
}