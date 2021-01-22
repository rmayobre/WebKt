package channel.tcp

import java.io.IOException
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NotYetBoundException

/**
 * All possible results of [SuspendedServerSocketChannel.accept].
 * @see SuspendedServerSocketChannel.accept
 */
sealed class AcceptResult {

    /**
     * SuspendedServerSocketChannel has successfully created a SuspendedSocketChannel connection.
     * @param channel the channel created as a result from the accept call.
     */
    data class Successful(val channel: SuspendedSocketChannel): AcceptResult()

    /**
     * The SuspendedServerSocketChannel's accept function failed because of an
     * Exception being thrown. Expected Exceptions to occur: [ClosedChannelException],
     * [AsynchronousCloseException], [ClosedByInterruptException], [NotYetBoundException],
     * [SecurityException], [IOException]
     * @param exception the exception thrown during the accept call.
     */
    data class Failure(val exception: Exception): AcceptResult()
}