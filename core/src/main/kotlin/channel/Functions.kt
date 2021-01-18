package channel

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