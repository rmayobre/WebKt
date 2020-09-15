package http.message.reader

import http.message.BadMessageException
import http.message.Message
import http.message.channel.buildMessage
import http.message.channel.splitHeader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Deprecated("Replaced with Message Channels")
class MessageInputStreamReader(
    private val input: InputStream
) : MessageReader {

    @Throws(
        IOException::class,
        BadMessageException::class)
    override fun read(): Message {
        val inputStreamReader = InputStreamReader(input)
        val bufferedReader = BufferedReader(inputStreamReader)

        val startLine = bufferedReader.readLine()
        val headers: MutableMap<String,String> = mutableMapOf()

        var line = bufferedReader.readLine()
        while (line.isNotEmpty()) {
            val h = line.splitHeader()
            headers[h[0]] = h[1]
            line = bufferedReader.readLine()
        }

        val length = headers["Content-Length"]?.toIntOrNull() ?: 0
        if (length > 0) {
            val bodyBuilder = StringBuilder()
            line = bufferedReader.readLine()
            while (line.isNotEmpty()) {
                bodyBuilder.append(line)
                line = bufferedReader.readLine()
            }
            return buildMessage(startLine, headers, bodyBuilder.toString())
        }

        return buildMessage(startLine, headers)
    }

    @Throws(
        IOException::class,
        TimeoutException::class,
        BadMessageException::class)
    override fun read(time: Int, unit: TimeUnit): Message {
        val timeout: Long = System.currentTimeMillis() + unit.toMillis(time.toLong())
        val inputStreamReader = InputStreamReader(input)
        val bufferedReader = BufferedReader(inputStreamReader)
        val headers: MutableMap<String,String> = mutableMapOf()

        val startLine: String = bufferedReader.readLine()
        if (System.currentTimeMillis() >= timeout) {
            throw TimeoutException("Could not read InputStream within time limit.")
        }

        var line: String? = bufferedReader.readLine()
        if (System.currentTimeMillis() >= timeout) {
            throw TimeoutException("Could not read InputStream within time limit.")
        }

        while (line != null && line.isNotEmpty()) {
            val h: List<String> = line.splitHeader()
            headers[h[0]] = h[1]
            line = bufferedReader.readLine()
            if (System.currentTimeMillis() >= timeout) {
                throw TimeoutException("Could not read InputStream within time limit.")
            }
        }

        val length: Int = headers["Content-Length"]?.toIntOrNull() ?: 0
        if (length > 0) {
            val bodyBuilder = StringBuilder()
            line = bufferedReader.readLine()
            while (line != null && line.isNotEmpty()) {
                bodyBuilder.append(line)
                line = bufferedReader.readLine()
            }
            return buildMessage(startLine, headers, bodyBuilder.toString())
        }

        return buildMessage(startLine, headers)
    }
}