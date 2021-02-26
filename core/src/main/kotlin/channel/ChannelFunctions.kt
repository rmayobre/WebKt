package channel

import engine.Attachment
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import javax.net.ssl.SSLEngine
import kotlin.jvm.Throws

internal fun TLSChannel.toString(engine: SSLEngine) =
        "SSL Session:       $session" +
        "Protocol:          ${session.protocol}\n" +
        "Cipher Suit:       ${session.cipherSuite}\n" +
        "Handshake Session: ${engine.handshakeSession}\n" +
        "Handshake Status:  ${engine.handshakeStatus}\n" +
        "Need Client Auth:  ${engine.needClientAuth}\n"

/**
 * Registers a SuspendedNetworkChannel to the Selector. Registry process works by registering
 * the SuspendedNetworkChannel's SelectableChannel with an Attachment class that wraps the
 * SuspendedNetworkChannel and the provided Attachment. To get the SuspendedNetworkChannel, during a
 * Selector's select (select, selectNow, etc), the SuspendedNetworkChannel will be provided
 * within the [SelectionKey.attachment]
 */
@Throws(IOException::class)
fun SuspendedNetworkChannel<*>.register(
    selector: Selector,
    operationFlag: Int,
    attachment: Any? = null
) {
    if (isOpen && selector.isOpen) {
        channel.register(
            selector,
            operationFlag,
            Attachment(
                channel = this,
                storage = attachment
            )
        )
    }
}