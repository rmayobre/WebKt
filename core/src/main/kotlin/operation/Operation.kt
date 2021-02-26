package operation

import channel.SuspendedNetworkChannel

sealed class Operation {

    abstract val channel: SuspendedNetworkChannel<*>

    abstract val attachment: Any?

    data class Accept(
        override val channel: SuspendedNetworkChannel<*>,
        override val attachment: Any? = null
    ): Operation()

    data class Connect(
        override val channel: SuspendedNetworkChannel<*>,
        override val attachment: Any? = null
    ): Operation()

    data class Read(
        override val channel: SuspendedNetworkChannel<*>,
        override val attachment: Any? = null)
        : Operation()

    data class Write(
        override val channel: SuspendedNetworkChannel<*>,
        override val attachment: Any? = null
    ): Operation()
}