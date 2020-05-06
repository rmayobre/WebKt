package http.message.reader

import http.Method
import http.Status
import http.message.BadMessageException
import http.message.Message
import http.message.Request
import http.message.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

        val startLine: String = bufferedReader.readLine()
        val headers: MutableMap<String,String> = mutableMapOf()

        var line: String = bufferedReader.readLine()
        if (System.currentTimeMillis() >= timeout) {
            throw TimeoutException("Could not read InputStream within time limit.")
        }

        while (line.isNotEmpty()) {
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
            while (line.isNotEmpty()) {
                bodyBuilder.append(line)
                line = bufferedReader.readLine()
            }
            return buildMessage(startLine, headers, bodyBuilder.toString())
        }

        return buildMessage(startLine, headers)
    }

    companion object {
        private val HEADER_REGEX = Regex(":\\s*")

        private val WHITESPACE_REGEX = Regex("\\s")

        /** Split a String into a list of Strings separated by whitespace. */
        private fun String.splitByWhitespace() = split(WHITESPACE_REGEX)

        private fun String.splitHeader() = split(HEADER_REGEX, 2)

        /**
         * Builds message into a Request or a Response. If the message cannot be built, this will throw
         * and exception
         * @throws BadMessageException when it cannot create a Request or Response object.
         * @see Response
         * @see Request
         */
        @Throws(BadMessageException::class)
        private fun buildMessage(
            startLine: String,
            headers: MutableMap<String, String>,
            body: String? = null
        ): Message {
            val lines = startLine.splitByWhitespace()
            return Method.find(lines[0])?.let { method ->
                Request(
                    path = lines[1],
                    method = method,
                    version = lines[2],
                    line = startLine,
                    headers = headers,
                    body = body
                )
            } ?: Status.find(lines[2])?.let { status ->
                Response(
                    status = status,
                    version = lines[0],
                    line = startLine,
                    headers = headers,
                    body = body
                )
            } ?: run {
                throw BadMessageException("Could not build request from provided data.")
            }
        }
    }
}