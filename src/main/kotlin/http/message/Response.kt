package http.message

import http.Status
import java.lang.Exception
import java.lang.IllegalArgumentException

data class Response(
    val status: Status,
    val version: String,
    override val line: String,
    override val headers: Map<String, String>,
    override val body: String? = null
): Message {
    companion object {
        private val WHITESPACE_REGEX = Regex("\\s")
        /** Split a String into a list of Strings separated by whitespace. */
        private fun String.splitByWhitespace() = split(WHITESPACE_REGEX)
    }
}