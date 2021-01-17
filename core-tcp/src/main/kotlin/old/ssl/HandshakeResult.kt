package old.ssl

import channel.tls.HandshakeResult

sealed class HandshakeResult {

    val isSuccessful: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this !is Success

    // TODO - Success value should be data relating to the handshake data.
    object Success : HandshakeResult()

    data class Failed(val message: String) : HandshakeResult()

    data class Error(val throwable: Throwable): HandshakeResult()
}
