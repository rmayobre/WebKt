package http.message.writer

import http.message.Message
import java.io.IOException

@Deprecated("Replaced with Message Channels")
interface MessageWriter {
    @Throws(IOException::class)
    fun write(message: Message)
}