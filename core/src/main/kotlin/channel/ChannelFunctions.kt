package channel

import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import javax.net.ssl.SSLEngine

internal fun SelectableChannelWrapper.toString(engine: SSLEngine): String =
    "SelectableChannelWrapper: ${hashCode()}\n" +
            "Channel Class:     ${channel.javaClass}\n" +
            "Remote Address:    $remoteAddress\n" +
            "Remote Port:       $remotePort\n" +
            "Local Address:     $localAddress\n" +
            "Local Port:        $localPort\n" +
            "Handshake Session: ${engine.handshakeSession}\n" +
            "Handshake Status:  ${engine.handshakeStatus}\n" +
            "Need Client Auth:  ${engine.needClientAuth}\n" +
            "Cipher Suit:       ${session.cipherSuite}\n" +
            "Protocol:          ${session.protocol}\n" +
            "SSL Session:       $session"

// Should this be public or internal?
suspend fun ByteChannel.readAsync(buffer: ByteBuffer): Int = coroutineScope<Int> {
    TODO("Not yet implemented") //return@coroutineScope 0
}

// Should this be public or internal?
suspend fun ByteChannel.writeAsync(buffer: ByteBuffer): Int = coroutineScope<Int> {
    TODO("Not yet implemented") //return@coroutineScope 0
}