package engine.operation

import channel.NetworkChannel
import java.nio.channels.Channel

sealed class Operation<T : NetworkChannel<*>>(
    val channel: T,
    val attachment: Any?
 ) {

    class Accept<T : NetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Connect<T : NetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Read<T : NetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Write<T : NetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)
}