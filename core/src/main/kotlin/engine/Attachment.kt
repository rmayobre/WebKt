package engine

import channel.SuspendedNetworkChannel
import java.nio.channels.SelectableChannel

data class Attachment<T : SelectableChannel>(
    val channel: SuspendedNetworkChannel<T>,
    val storage: Any? = null
)