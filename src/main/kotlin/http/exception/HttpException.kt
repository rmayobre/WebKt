package http.exception

import http.Status
import java.lang.Exception

open class HttpException : Exception {
    val status: Status
    constructor(status: Status) : super() { this.status = status }
    constructor(cause: Throwable, status: Status) : super(cause) { this.status = status }
    constructor(message: String, status: Status) : super(message) { this.status = status }
    constructor(message: String, cause: Throwable, status: Status) : super(message, cause) { this.status = status }

    fun isInformation(): Boolean = status.isInformation()

    fun isSuccessful(): Boolean = status.isSuccessful()

    fun isRedirection(): Boolean = status.isRedirection()

    fun isClientError(): Boolean = status.isClientError()

    fun isServerError(): Boolean = status.isServerError()

    fun isError(): Boolean = status.isError()

}

// TODO make a bad request exception