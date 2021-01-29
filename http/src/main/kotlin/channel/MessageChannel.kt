package channel

import channel.tcp.SuspendedSocketChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import message.BadMessageException
import message.Message
import message.headersToString
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.nio.ByteBuffer

class MessageChannel(
    private val channel: SuspendedSocketChannel,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
) {

    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

    suspend fun read(): Message {
        val headers: MutableMap<String, String> = mutableMapOf()
        val buffer = byteBuffer

        val requestLine: String = channel.readRequestLine(buffer)
        var line: String? = channel.readLine(buffer)

        while (line != null && line.isNotEmpty()) {
            val (key: String, value: String) = line.split(message.channel.HEADER_REGEX, 2)
            headers[key] = value
            line = channel.readLine(buffer)
        }

        val length: Int = headers["Content-Length"]?.toIntOrNull() ?: 0

        if (length > 0) {
            val builder = StringBuilder()
            line = channel.readLine(buffer)
            while (line != null && line.isNotEmpty()) {
                builder.append(line)
                line = channel.readLine(buffer)
            }
            return message.channel.buildMessage(requestLine, headers, channel.toString())
        }

        return message.channel.buildMessage(requestLine, headers)
    }

    suspend fun write(message: Message) {
        coroutineScope {
            val job = async(Dispatchers.IO) {
                ByteArrayOutputStream().apply {
                    write((message.line + "\n").toByteArray())
                    write(message.headersToString().toByteArray())
                    write("\r\n".toByteArray())
                    message.body?.let { body ->
                        write(body.toByteArray())
                        write("\r\n".toByteArray())
                    }
                }
            }
            channel.write(
                buffer = ByteBuffer.wrap(job.await().toByteArray())
            )
        }
    }

    companion object {
        /**
         * Read the first line of a request.
         * @throws BadMessageException if data is broken, or first line could not be constructed into a request line.
         * @return Request line (first line of request) as a String.
         */
        @Throws(BadMessageException::class)
        private fun SuspendedByteChannel.readRequestLine(buffer: ByteBuffer): String {
            if (read(buffer) == -1) {
                throw BadMessageException("Could not read request.")
            } else {
                buffer.flip()
                return readLine(buffer) ?: throw BadMessageException("An empty request was sent.")
            }
        }

    }
}