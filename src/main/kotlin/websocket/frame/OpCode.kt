package websocket.frame

import websocket.InvalidFrameException

/**
 * Declaration of a websocket.frame type. OpCodes are used to specify what kind of
 * data or control is in the websocket.frame.
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.8">RFC 6455, Section 11.8 (WebSocket Opcode Registry)</a>
 */
enum class OpCode(val code: Byte) {
    /** WebSocket websocket.frame.OpCode for a Continuation websocket.frame.Frame */
    CONTINUATION(0x00),
    /** WebSocket websocket.frame.OpCode for a Text websocket.frame.Frame */
    TEXT(0x01),
    /** WebSocket websocket.frame.OpCode for a Binary websocket.frame.Frame */
    BINARY(0x02),
    /** WebSocket websocket.frame.OpCode for a Close websocket.frame.Frame */
    CLOSE(0x08),
    /** WebSocket websocket.frame.OpCode for a Ping websocket.frame.Frame */
    PING(0x09),
    /** WebSocket websocket.frame.OpCode for a Pong websocket.frame.Frame */
    PONG(0x0A);

    val isData: Boolean = code in 0x00..0x02

    val isControl: Boolean = code in 0x08..0x0A

    companion object {
        @Throws(InvalidFrameException::class)
        fun find(code: Int): OpCode =
            find(code.toByte())

        /**
         * Find websocket.frame.OpCode by byte.
         *
         * @param code data stream's given opcode.
         * @throws InvalidFrameException Thrown if websocket.frame.OpCode was not recognized.
         * @see OpCode
         */
        @Throws(InvalidFrameException::class)
        fun find(code: Byte): OpCode = when (code) {
            CONTINUATION.code -> CONTINUATION
            TEXT.code -> TEXT
            BINARY.code -> BINARY
            CLOSE.code -> CLOSE
            PING.code -> PING
            PONG.code -> PONG
            else -> throw InvalidFrameException()
        }
    }
}