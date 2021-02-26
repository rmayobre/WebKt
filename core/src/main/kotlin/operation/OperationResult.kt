package operation

import channel.tcp.SuspendedSocketChannel
import java.io.IOException
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NotYetBoundException

/**
 * All possible results of [SuspendedServerSocketChannel.accept].
 * @see SuspendedServerSocketChannel.accept
 */
@Deprecated("remove")
sealed class OperationResult<T> {

    /**
     * SuspendedServerSocketChannel has successfully created a SuspendedSocketChannel connection.
     * @param channel the channel created as a result from the accept call.
     */
    open class Successful<T>(open val value: T): OperationResult<T>()

    /**
     * The SuspendedServerSocketChannel's accept function failed because of an
     * Exception being thrown. Expected Exceptions to occur: [ClosedChannelException],
     * [AsynchronousCloseException], [ClosedByInterruptException], [NotYetBoundException],
     * [SecurityException], [IOException]
     * @param exception the exception thrown during the accept call.
     */
    open class Failure(val exception: Exception): OperationResult<Exception>()
}

sealed class AcceptResult : OperationResult<SuspendedSocketChannel>() {
    data class Successful(override val value: SuspendedSocketChannel): OperationResult.Successful<SuspendedSocketChannel>(value)
}