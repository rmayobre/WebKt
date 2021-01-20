package engine.operation

import kotlinx.coroutines.channels.SendChannel
import java.nio.channels.SelectableChannel


// Maybe make this a coroutine?
class OperationsChannel(
    private val channel: SendChannel<OperationMessage>
) {

    suspend fun accept(selectableChannel: SelectableChannel, attachment: Any? = null) =
        channel.send(OperationMessage.Accept(selectableChannel, attachment))

    suspend fun connect(selectableChannel: SelectableChannel, attachment: Any? = null) =
        channel.send(OperationMessage.Connect(selectableChannel, attachment))

    suspend fun read(selectableChannel: SelectableChannel, attachment: Any? = null) =
        channel.send(OperationMessage.Read(selectableChannel, attachment))

    suspend fun write(selectableChannel: SelectableChannel, attachment: Any? = null) =
        channel.send(OperationMessage.Write(selectableChannel, attachment))

}