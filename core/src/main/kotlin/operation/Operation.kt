package operation

import channel.SuspendedNetworkChannel

sealed class Operation<T : SuspendedNetworkChannel<*>>(
    val channel: T,
    val attachment: Any?
 ) {

    class Accept<T : SuspendedNetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Connect<T : SuspendedNetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Read<T : SuspendedNetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)

    class Write<T : SuspendedNetworkChannel<*>>(channel: T, attachment: Any? = null): Operation<T>(channel, attachment)
}