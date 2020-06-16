package http.exception

import http.Status
import http.message.Response
import java.lang.Exception
import java.util.*

open class HttpException private constructor(
    val status: Status,
    val reason: String,
    var body: String,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    val headers: MutableMap<String, String> = mutableMapOf()

    val response: Response
        get() = Response.Builder(status)
            .addHeaders(headers)
            .addHeader("Content-Length", body.length.toString())
            .setBody(body)
            .build()

    init {
        headers["Content-Type"] = DEFAULT_CONTENT_TYPE
        headers["Connection"] = "close"
    }

    constructor(status: Status, cause: Throwable) : this(status, "", cause)

    constructor(status: Status, reason: String = "", cause: Throwable? = null) : this(
        status = status,
        reason = reason,
        body = StringBuilder().apply {
            append("{")
            append("\"timestamp\" : ${Date().time},")
            append("\"status\" : ${status.code},")
            append("\"message\" : \"${status.message}\",")
            append("\"reason\" : \"$reason\"")
            append("}")
        }.toString(),
        cause = cause
    )

    constructor(status: Status, body: String, reason: String = "", cause: Throwable? = null) : this(
        status = status,
        reason = reason,
        body = body,
        message = status.message,
        cause = cause
    )

    companion object {
        private const val DEFAULT_CONTENT_TYPE = "application/json"
    }
}

class BadRequestException : HttpException {
    constructor() : super(Status.BAD_REQUEST)
    constructor(cause: Throwable) : super(Status.BAD_REQUEST, cause)
    constructor(reason: String) : super(Status.BAD_REQUEST, reason)
    constructor(reason: String, cause: Throwable) : super(Status.BAD_REQUEST, reason, cause)
}

class NotFoundException : HttpException {
    constructor() : super(Status.NOT_FOUND)
    constructor(cause: Throwable) : super(Status.NOT_FOUND, cause)
    constructor(reason: String) : super(Status.NOT_FOUND, reason)
    constructor(reason: String, cause: Throwable) : super(Status.NOT_FOUND, reason, cause)
}