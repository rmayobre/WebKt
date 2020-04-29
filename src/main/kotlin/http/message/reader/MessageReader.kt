package http.message.reader

import exception.BadRequestException
import http.message.Message
import java.io.IOException

interface MessageReader {
    @Throws(IOException::class, BadRequestException::class)
    fun read(): Message
}