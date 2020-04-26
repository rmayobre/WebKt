package exception

import ClosureCode
import java.io.UnsupportedEncodingException

/**
 * Base exception for WebSocket connectivity issues. WebSocket exceptions follow the RFC 6455
 * websocket protocol and result in the websocket connection being drop.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455 WebSocket Protocol</a>
 */
open class WebsocketException : Exception {
    val code: ClosureCode

    constructor(message: String, code: ClosureCode):
            super(message) { this.code = code }

    constructor(message: String, cause: Throwable, code: ClosureCode):
            super(message, cause) { this.code = code }

    constructor(cause: Throwable, code: ClosureCode):
            super(cause) { this.code = code }
}

/**
 * Exception for any problems with building or using websocket Data frames.
 * Must follow the <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a> guidelines.
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>
 * @see ClosureCode.POLICY_VALIDATION
 */
class InvalidFrameException: WebsocketException{
    constructor() : super("Invalid OpCode found in frame.", ClosureCode.POLICY_VALIDATION)
    constructor(message: String) : super(message, ClosureCode.POLICY_VALIDATION)
}

/**
 * Something went wrong with a Frame's fragmentation.
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.4">RFC 6455 - Fragmentation</a>
 */
class BadFragmentException: WebsocketException("A malformed fragment was found.", ClosureCode.PROTOCOL_ERROR)

/**
 * There was an IO related issue (e.g. Input and output streams were not connected, initialized,
 * or failed to connect to endpoint). This exception results in an {@link StatusCode#ABNORMAL_CLOSE}
 * and should not be sent to opposing endpoint.
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>
 * @see ClosureCode.ABNORMAL_CLOSE
 */
class WebsocketIOException: WebsocketException {
    constructor(cause: Throwable) : super("Unexpected error occurred while reading an endpoint's stream", cause, ClosureCode.ABNORMAL_CLOSE)
    constructor(message: String, cause: Throwable) : super(message, cause, ClosureCode.ABNORMAL_CLOSE)
}

/**
 * Client is required to Mask each fragment of the frame, if specified.
 */
class MissingMaskFragmentException : WebsocketException("Client did not send a masked fragment, when required.", ClosureCode.PROTOCOL_ERROR)

/**
 * Exception when a WebSocket connection failed to perform TLS opening handshake.
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-4">RFC 6455, Section 4 Opening Handshake</a>
 * @see ClosureCode.TLS_ERROR
 */
class HandshakeException : WebsocketException {
    constructor(message: String): super(message, ClosureCode.TLS_ERROR)
    constructor(message: String, cause: Throwable): super(message, cause, ClosureCode.TLS_ERROR)
}

class NoUTFException(exception: UnsupportedEncodingException):
    WebsocketException("Text frame did not support UTF-8 character set.", exception, ClosureCode.NO_UTF8)

class LargeFrameException(limit: Int):
    WebsocketException("Frame(s) exceeds size limit of $limit bytes.", ClosureCode.TOO_BIG)

class InternalErrorException : WebsocketException {
    constructor(message: String) : super(message, ClosureCode.INTERNAL_ERROR)
    constructor(throwable: Throwable) : super(throwable, ClosureCode.INTERNAL_ERROR)
    constructor(message: String, throwable: Throwable) : super(message, throwable, ClosureCode.INTERNAL_ERROR)
}

/** This exception is thrown without a Session because this exception can only occur before a Session has been created. */
class BadRequestException : WebsocketException {
    constructor() : super("Request could not be determined from endpoint.", ClosureCode.PROTOCOL_ERROR)
    constructor(message: String) : super(message, ClosureCode.PROTOCOL_ERROR)
    constructor(message: String, cause: Throwable) : super(message, cause, ClosureCode.PROTOCOL_ERROR)
}