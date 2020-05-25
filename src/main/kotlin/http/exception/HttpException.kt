package http.exception

import http.Status
import http.message.Response
import java.lang.Exception
import java.util.*

open class HttpException : Exception {

    val status: Status

    val reason: String

    val headers: MutableMap<String, String> = mutableMapOf()
        get() = field.apply {
            put("Content-Length", body.length.toString())
        }

    var body: String

    val response: Response
        get() = Response.Builder(status)
            .addHeader("Content-Type", DEFAULT_CONTENT_TYPE)
            .addHeader("Content-Length", body.length.toString())
            .addHeader("Connection", "close")
            .setBody(body)
            .build()

    constructor(status: Status) : super() {
        this.status = status
        reason = ""
        body = StringBuilder().apply {
            append("{")
            append("\"timestamp\" : ${Date().time},")
            append("\"status\" : ${status.code},")
            append("\"message\" : \"${status.message}\",")
            append("\"reason\" : \"$reason\"")
            append("}")
        }.toString()
        headers["Content-Type"] = DEFAULT_CONTENT_TYPE
        headers["Connection"] = "close"
    }

    constructor(cause: Throwable, status: Status) : super(cause) {
        this.status = status
        reason = ""
        body = StringBuilder().apply {
            append("{")
            append("\"timestamp\" : ${Date().time},")
            append("\"status\" : ${status.code},")
            append("\"message\" : \"${status.message}\",")
            append("\"reason\" : \"$reason\"")
            append("}")
        }.toString()
        headers["Content-Type"] = DEFAULT_CONTENT_TYPE
        headers["Connection"] = "close"
    }

    constructor(message: String, status: Status) : super(message) {
        this.status = status
        reason = message
        body = StringBuilder().apply {
            append("{")
            append("\"timestamp\" : ${Date().time},")
            append("\"status\" : ${status.code},")
            append("\"message\" : \"${status.message}\",")
            append("\"reason\" : \"$reason\"")
            append("}")
        }.toString()
        headers["Content-Type"] = DEFAULT_CONTENT_TYPE
        headers["Connection"] = "close"
    }

    constructor(message: String, cause: Throwable, status: Status) : super(message, cause) {
        this.status = status
        reason = message
        body = StringBuilder().apply {
            append("{")
            append("\"timestamp\" : ${Date().time},")
            append("\"status\" : ${status.code},")
            append("\"message\" : \"${status.message}\",")
            append("\"reason\" : \"$reason\"")
            append("}")
        }.toString()
        headers["Content-Type"] = DEFAULT_CONTENT_TYPE
        headers["Connection"] = "close"
    }

    companion object {
        private const val DEFAULT_CONTENT_TYPE = "application/json"
    }
}

class BadRequestException : HttpException {
//    val request: Request?
    constructor() : super(Status.BAD_REQUEST)
    constructor(cause: Throwable) : super(cause, Status.BAD_REQUEST)
    constructor(message: String) : super(message, Status.BAD_REQUEST)
    constructor(message: String, cause: Throwable) : super(message, cause, Status.BAD_REQUEST)
}