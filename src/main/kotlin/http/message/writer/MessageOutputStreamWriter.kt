package http.message.writer

import http.message.Message
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

@Deprecated("Replaced with Message Channels")
class MessageOutputStreamWriter(
    private val output: OutputStream
) : MessageWriter {
    @Throws(IOException::class)
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