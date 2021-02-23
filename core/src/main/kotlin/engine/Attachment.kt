package engine

import channel.SuspendedNetworkChannel
import java.nio.channels.SelectableChannel

data class Attachment<T : SelectableChannel>(
    val channel: SuspendedNetworkChannel<T>,
    val storage: Any? = null
)

inline fun <reified T : SuspendedNetworkChannel<*>> Attachment<*>.toTypeOf(
    block: (channel: T, attachment: Any?) -> Unit
) {
    if (channel is T) {
        block(channel, storage)
    }
}