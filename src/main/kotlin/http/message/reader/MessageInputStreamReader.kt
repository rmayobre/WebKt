package http.message.reader

import exception.BadRequestException
import http.Method
import http.Status
import http.message.Message
import http.message.Request
import http.message.Response
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class MessageInputStreamReader(
    private val input: InputStream
) : MessageReader {
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

        // TODO read the body of the request.

        return build(startLine, headers)
    }

    private fun build(
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
            throw BadRequestException("Could not build request from provided data.")
        }
    }

    companion object {
        private val HEADER_REGEX = Regex(":\\s*")

        private val WHITESPACE_REGEX = Regex("\\s")

        /** Split a String into a list of Strings separated by whitespace. */
        private fun String.splitByWhitespace() = split(WHITESPACE_REGEX)

        private fun String.splitHeader() = split(HEADER_REGEX, 2)
    }
}