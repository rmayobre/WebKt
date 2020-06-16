package http.exception

import java.lang.Exception

interface ExceptionHandler {
    fun onException(ex: Exception)
}