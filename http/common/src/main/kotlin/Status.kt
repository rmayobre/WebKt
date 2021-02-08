import java.lang.IllegalArgumentException

enum class Status(
    val code: Int,
    val message: String,
    private val type: Int
) {

    //
    // 1xx: Information
    //

    CONTINUE(100, "Continue", 1),
    SWITCH_PROTOCOL(101, "Switching Protocols", 1),

    //
    // 2xx: Success
    //

    OK(200, "OK", 2),
    CREATED(201, "Created", 2),
    ACCEPTED(202, "Accepted", 2),
    NO_CONTENT(204, "No Content", 2),
    PARTIAL_CONTENT(206, "Partial Content", 2),
    MULTI_STATUS(207, "Multi-Status", 2),

    //
    // 3xx: Redirection
    //

    MOVED_PERMANENTLY(301, "Moved Permanently", 3),
    FOUND(302, "Found", 3),
    SEE_OTHER(303, "See Other", 3),
    NOT_MODIFIED(304, "Not Modified", 3),
    TEMPORARY_REDIRECT(307, "Temporary Redirect", 3),

    //
    // 4xx: Client Error
    //

    BAD_REQUEST(400, "Bad Request", 4),
    UNAUTHORIZED(401, "Unauthorized", 4),
    FORBIDDEN(403, "Forbidden", 4),
    NOT_FOUND(404, "Not Found", 4),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", 4),
    NOT_ACCEPTABLE(406, "Not Acceptable", 4),
    REQUEST_TIMEOUT(408, "Request Timeout", 4),
    CONFLICT(409, "Conflict", 4),
    GONE(410, "Gone", 4),
    LENGTH_REQUIRED(411, "Length Required", 4),
    PRECONDITION_FAILED(412, "Precondition Failed", 4),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large", 4),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", 4),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable", 4),
    EXPECTATION_FAILED(417, "Expectation Failed", 4),
    TOO_MANY_REQUESTS(429, "Too Many Requests", 4),

    //
    // 5xx: Server Error
    //

    INTERNAL_SERVER_ERROR(500, "Internal Server Error", 5),
    NOT_IMPLEMENTED(501, "Not Implemented", 5),
    SERVICE_UNAVAILABLE(503, "Service Unavailable", 5),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", 5);

    fun isInformation(): Boolean = type == INFORMATIONAL

    fun isSuccessful(): Boolean = type == SUCCESSFUL

    fun isRedirection(): Boolean = type == REDIRECT

    fun isClientError(): Boolean = type == CLIENT_ERROR

    fun isServerError(): Boolean = type == SERVER_ERROR

    fun isError(): Boolean = type == CLIENT_ERROR || type == SERVER_ERROR

    companion object {

        private const val INFORMATIONAL = 1

        private const val SUCCESSFUL = 2

        private const val REDIRECT = 3

        private const val CLIENT_ERROR = 4

        private const val SERVER_ERROR = 5

        fun find(message: String): Status? = values().find { it.message == message }

        fun find(code: Int): Status =
            values().find { it.code == code }
            ?: throw IllegalArgumentException("Unrecognized HTTP status.")
    }
}