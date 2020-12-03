package message.reader

import message.*
import message.channel.HEADER_REGEX
import message.channel.buildMessage
import message.channel.readLine
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Deprecated("Replaced with Message Channels")
class MessageBufferReader(
    private val channel: SocketChannel,
    bufferSize: Int
) : MessageReader {

    private val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

    override fun read(): Message {
        val headers: MutableMap<String, String> = mutableMapOf()

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

    companion object {
        @Throws(BadMessageException::class)
        private fun SocketChannel.readRequestLine(buffer: ByteBuffer): String {
            if (read(buffer) == -1) {
                throw BadMessageException("Could not read request.")
            } else {
                buffer.flip()
                return readLine(buffer) ?: throw BadMessageException("An empty request was sent.")
            }
        }
    }
}