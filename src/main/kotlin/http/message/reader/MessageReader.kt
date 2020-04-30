package http.message.reader

import http.exception.BadMessageException
import http.message.Message
import java.io.IOException

interface MessageReader {
    @Throws(IOException::class, BadMessageException::class)
    fun read(): Message
}