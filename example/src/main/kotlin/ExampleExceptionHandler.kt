package example

import http.exception.ExceptionHandler
import java.lang.Exception

class ExampleExceptionHandler : ExceptionHandler {
    override fun onException(ex: Exception) {
        println("An exception occurred...")
        println("Exception (message): ${ex.message}")
        println("Exception (cause): ${ex.cause}")
    }
}