package http.message.channel.factory

import http.message.*
import http.message.channel.HEADER_REGEX
import http.message.channel.buildMessage
import http.message.channel.MessageChannel
import http.message.channel.readLine
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.Channel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class DefaultMessageChannelFactory(
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : MessageChannelFactory {
    override fun create(channel: ByteChannel): MessageChannel =
        DefaultMessageChannel(channel, bufferSize)

    private class DefaultMessageChannel(
        private val channel: ByteChannel,
        bufferSize: Int
    ) : MessageChannel, Channel by channel {

        private var byteBuffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

        override fun read(): Message {
            val headers: MutableMap<String, String> = mutableMapOf()
            val buffer = byteBuffer

            val requestLine: String = channel.readRequestLine(buffer)
            var line: String? = channel.readLine(buffer)

            while (line != null && line.isNotEmpty()) {
                val (key: String, value: String) = line.split(HEADER_REGEX, 2)
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
                return buildMessage(requestLine, headers, channel.toString())
            }

            return buildMessage(requestLine, headers)
        }


        override fun read(time: Int, unit: TimeUnit): Message {
            val timeout: Long = System.currentTimeMillis() + unit.toMillis(time.toLong())
            val headers: MutableMap<String, String> = mutableMapOf()
            val buffer = byteBuffer

            val requestLine: String = channel.readRequestLine(buffer)
            if (System.currentTimeMillis() >= timeout) {
                throw TimeoutException("Could not read channel within time limit.")
            }

            var line: String? = channel.readLine(buffer)
            if (System.currentTimeMillis() >= timeout) {
                throw TimeoutException("Could not read channel within time limit.")
            }

            while (line != null && line != "") {
                val (key: String, value: String) = line.split(HEADER_REGEX, 2)
                headers[key] = value
                line = channel.readLine(buffer)
                if (System.currentTimeMillis() >= timeout) {
                    throw TimeoutException("Could not read channel within time limit.")
                }
            }

            val length: Int = headers["Content-Length"]?.toIntOrNull() ?: 0

            if (length > 0) {
                val builder = StringBuilder()

                line = channel.readLine(buffer)
                if (System.currentTimeMillis() >= timeout) {
                    throw TimeoutException("Could not read channel within time limit.")
                }

                while (line != null && line.isNotEmpty()) {
                    builder.append(line)
                    line = channel.readLine(buffer)
                    if (System.currentTimeMillis() >= timeout) {
                        throw TimeoutException("Could not read channel within time limit.")
                    }
                }
                return buildMessage(requestLine, headers, channel.toString())
            }

            return buildMessage(requestLine, headers)
        }

        override fun write(data: Message): Int {
            val output: ByteArrayOutputStream = ByteArrayOutputStream().apply {
                write((data.line + "\n").toByteArray())
                write(data.headersToString().toByteArray())
                write("\r\n".toByteArray())
                data.body?.let { body ->
                    write(body.toByteArray())
                    write("\r\n".toByteArray())
                }
            }
            return channel.write(ByteBuffer.wrap(output.toByteArray()))
        }

        companion object {

            /**
             * Read the first line of a request.
             * @throws BadMessageException if data is broken, or first line could not be constructed into a request line.
             * @return Request line (first line of request) as a String.
             */
            @Throws(BadMessageException::class)
            private fun ByteChannel.readRequestLine(buffer: ByteBuffer): String {
                if (read(buffer) == -1) {
                    throw BadMessageException("Could not read request.")
                } else {
                    buffer.flip()
                    return readLine(buffer) ?: throw BadMessageException("An empty request was sent.")
                }
            }

            /**
             * Convert the headers map from Message into a String.
             * @return a String of a Message's headers.
             */
            private fun Message.headersToString(): String {
                val builder = StringBuilder()
                headers.forEach { (key: String, value: String) ->
                    builder.append("$key : $value\n")
                }
                return builder.toString()
            }
        }
    }
}