package operation

import channel.SuspendedNetworkChannel
import kotlinx.coroutines.channels.SendChannel

class OperationsChannel(
    private val channel: SendChannel<Operation>
) {
    suspend fun accept(channel: SuspendedNetworkChannel<*>, attachment: Any? = null) =
        this.channel.send(Operation.Accept(channel, attachment))

    suspend fun connect(channel: SuspendedNetworkChannel<*>, attachment: Any? = null) =
        this.channel.send(Operation.Connect(channel, attachment))

    suspend fun read(channel: SuspendedNetworkChannel<*>, attachment: Any? = null) =
        this.channel.send(Operation.Read(channel, attachment))

    suspend fun write(channel: SuspendedNetworkChannel<*>, attachment: Any? = null) =
        this.channel.send(Operation.Write(channel, attachment))
}