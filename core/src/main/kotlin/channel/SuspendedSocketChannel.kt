package channel

import kotlinx.coroutines.channels.ActorScope
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlinx.coroutines.channels.actor

class SuspendedSocketChannel(
    val channel: SocketChannel
) : SuspendedByteChannel {

    private val networkActor = actor<SuspendedOperation> {

        val channel = ActorScope
    }

    override suspend fun read(buffer: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override suspend fun write(buffer: ByteBuffer): Int {

        TODO("Not yet implemented")
    }


    private sealed class SuspendedOperation {
        inner class Read(buffer: ByteBuffer) : SuspendedOperation()
        inner class Write(buffer: ByteBuffer) : SuspendedOperation()
    }
}