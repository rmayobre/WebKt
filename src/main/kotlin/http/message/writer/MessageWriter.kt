package http.message.writer

import http.message.Message
import java.io.IOException

interface MessageWriter {
    @Throws(IOException::class)
    fun write(message: Message)
}