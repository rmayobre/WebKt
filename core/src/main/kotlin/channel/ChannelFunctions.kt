package channel

import channel.tls.TLSChannel
import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine

internal fun TLSChannel.toString(engine: SSLEngine) =
        "SSL Session:       $session" +
        "Protocol:          ${session.protocol}\n" +
        "Cipher Suit:       ${session.cipherSuite}\n" +
        "Handshake Session: ${engine.handshakeSession}\n" +
        "Handshake Status:  ${engine.handshakeStatus}\n" +
        "Need Client Auth:  ${engine.needClientAuth}\n"

// Should this be public or internal?
suspend fun SuspendedByteChannel.readFlow(buffer: ByteBuffer): Int = coroutineScope<Int> {
    TODO("Not yet implemented") //return@coroutineScope 0
}

//// Should this be public or internal?
//suspend fun ByteChannel.writeAsync(buffer: ByteBuffer): Int = coroutineScope<Int> {
//    TODO("Not yet implemented") //return@coroutineScope 0
//}