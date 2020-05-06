package http.message

import java.lang.RuntimeException

class BadMessageException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}