package http.message.writer

import http.message.Message
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class MessageBufferWriter(
    private val channel: SocketChannel
) : MessageWriter {

    override fun write(message: Message) {
        val output: ByteArrayOutputStream = ByteArrayOutputStream().apply {
            write(message.line.toByteArray())
            write(message.headersToString().toByteArray())
            message.body?.let { body -> write(body.toByteArray()) }
        }
        channel.write(ByteBuffer.wrap(output.toByteArray()))
    }

    companion object {
        private fun Message.headersToString(): String {
            val builder = StringBuilder()
            headers.forEach { (key: String, value: String) ->
                builder.append("$key : $value")
            }
            return builder.toString()
        }
    }
}