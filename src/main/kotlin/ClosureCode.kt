enum class ClosureCode(val value: Int) {
    /**
     * 1000 indicates a normal closure, meaning that the purpose for
     * which the connection was established has been fulfilled.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    NORMAL(1000),
    /**
     * 1001 indicates that an endpoint is "going away", such as a server
     * going down or a browser having navigated away from a page.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    GOING_AWAY(1001),
    /**
     * 1002 indicates that an endpoint is terminating the connection due
     * to a protocol error.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    PROTOCOL_ERROR(1002),
    /**
     * 1003 indicates that an endpoint is terminating the connection
     * because it has received a type of data it cannot accept (e.g. an
     * endpoint that understands only text data MAY send this if it
     * receives a binary message).
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    UNSUPPORTED_DATA(1003),
    /**
     * 1005 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that no status
     * code was actually present.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    NO_STATUS(1005),
    /**
     * 1006 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the
     * connection was closed abnormally, e.g. without sending or
     * receiving a Close control frame.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    ABNORMAL_CLOSE(1006),
    /**
     * 1007 indicates that an endpoint is terminating the connection
     * because it has received data within a message that was not
     * consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
     * data within a text message).
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    NO_UTF8(1007),
    /**
     * 1008 indicates that an endpoint is terminating the connection
     * because it has received a message that violates its policy. This
     * is a generic status code that can be returned when there is no
     * other more suitable status code (e.g. 1003 or 1009), or if there
     * is a need to hide specific details about the policy.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    POLICY_VALIDATION(1008),
    /**
     * 1009 indicates that an endpoint is terminating the connection
     * because it has received a message which is too big for it to
     * process.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    TOO_BIG(1009),
    /**
     * 1010 indicates that an endpoint (client) is terminating the
     * connection because it has expected the server to negotiate one or
     * more extension, but the server didn't return them in the response
     * message of the WebSocket handshake. The list of extensions which
     * are needed SHOULD appear in the /reason/ part of the Close frame.
     * Note that this status code is not used by the server, because it
     * can fail the WebSocket handshake instead.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     */
    MISSING_EXTENSION(1010),
    /**
     * 1011 indicates that a server is terminating the connection because
     * it encountered an unexpected condition that prevented it from
     * fulfilling the request.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     **/
    INTERNAL_ERROR(1011),
    /**
     * 1015 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the
     * connection was closed due to a failure to perform a TLS handshake
     * (e.g., the server certificate can't be verified).
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 (Defined Status Codes)</a>
     **/
    TLS_ERROR(1015),

    /** Received an unknown error code. */
    UNKNOWN(-1);

    companion object {
        private val map: Map<Int, ClosureCode> = values().map { it.value to it }.toMap()

        /** Find ClosureCode from Integer. If code is not found, default value is UNKNOWN(-1). */
        fun find(code: Int): ClosureCode = map[code] ?: UNKNOWN
    }
}