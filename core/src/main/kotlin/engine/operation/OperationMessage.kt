package engine.operation

import java.nio.channels.SelectableChannel

sealed class OperationMessage(val channel: SelectableChannel, val attachment: Any?) {
    class Accept(channel: SelectableChannel, attachment: Any? = null): OperationMessage(channel, attachment)

    class Connect(channel: SelectableChannel, attachment: Any? = null): OperationMessage(channel, attachment)

    class Read(channel: SelectableChannel, attachment: Any? = null): OperationMessage(channel, attachment)

    class Write(channel: SelectableChannel, attachment: Any? = null): OperationMessage(channel, attachment)
}