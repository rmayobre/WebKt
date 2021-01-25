package operation

import channel.NetworkChannel
import kotlinx.coroutines.channels.SendChannel

class OperationsChannel<T : NetworkChannel<*>>(
    private val channel: SendChannel<Operation<T>>
) {

    suspend fun accept(channel: T, attachment: Any? = null) =
        this.channel.send(Operation.Accept(channel, attachment))

    suspend fun connect(channel: T, attachment: Any? = null) =
        this.channel.send(Operation.Connect(channel, attachment))

    suspend fun read(channel: T, attachment: Any? = null) =
        this.channel.send(Operation.Read(channel, attachment))

    suspend fun write(channel: T, attachment: Any? = null) =
        this.channel.send(Operation.Write(channel, attachment))
}