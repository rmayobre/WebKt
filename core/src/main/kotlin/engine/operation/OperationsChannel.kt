package engine.operation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel


// Maybe make this a coroutine?
class OperationsChannel(
    channel: Channel<OperationMessage>
) : Channel<OperationMessage> by channel {
// TODO iterate the channel to keep the coroutine alive!

    constructor(
        capacity: Int = Channel.RENDEZVOUS,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
        onUndeliveredElement: ((OperationMessage) -> Unit)? = null
    ) : this(
        channel = Channel(capacity, onBufferOverflow, onUndeliveredElement)
    )

}