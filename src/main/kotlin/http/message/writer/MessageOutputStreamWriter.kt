package http.message.writer

import http.message.Message
import java.io.OutputStream
import java.io.OutputStreamWriter

class MessageOutputStreamWriter(
    private val output: OutputStream
) : MessageWriter {
    override fun write(message: Message) {
        val writer = OutputStreamWriter(output, Charsets.UTF_8)
        val builder = StringBuilder("${message.line}\r\n")

        // Fetch headers.
        message.headers.forEach { (key, value) ->
            builder.append("$key: $value\r\n")
        }
        builder.append("\r\n")

        // Fetch body, if there is a body.
        message.body?.let { body -> builder.append(body)}

        writer.write(builder.toString())
    }
}