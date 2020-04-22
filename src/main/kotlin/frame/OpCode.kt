package frame

import exception.InvalidFrameException

/**
 * Declaration of a frame type. OpCodes are used to specify what kind of
 * data or control is in the frame.
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.8">RFC 6455, Section 11.8 (WebSocket Opcode Registry)</a>
 */
enum class OpCode(val code: Byte) {
    /** WebSocket frame.OpCode for a Continuation frame.Frame */
    CONTINUATION(0x00),
    /** WebSocket frame.OpCode for a Text frame.Frame */
    TEXT(0x01),
    /** WebSocket frame.OpCode for a Binary frame.Frame */
    BINARY(0x02),
    /** WebSocket frame.OpCode for a Close frame.Frame */
    CLOSE(0x08),
    /** WebSocket frame.OpCode for a Ping frame.Frame */
    PING(0x09),
    /** WebSocket frame.OpCode for a Pong frame.Frame */
    PONG(0x0A);

    val isData: Boolean = code in 0x00..0x02

    val isControl: Boolean = code in 0x08..0x0A

    companion object {
        @Throws(InvalidFrameException::class)
        fun find(code: Int): OpCode =
            find(code.toByte())

        /**
         * Find frame.OpCode by byte.
         *
         * @param code data stream's given opcode.
         * @throws InvalidFrameException Thrown if frame.OpCode was not recognized.
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