package http.message.writer

import http.message.Message
import java.io.OutputStream

class MessageOutputStreamWriter(
    private val output: OutputStream
) : MessageWriter {
    override fun write(message: Message) {
        TODO("Not yet implemented")
    }
}