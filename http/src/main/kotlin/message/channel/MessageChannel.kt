package message.channel

import Method
import Status
import message.BadMessageException
import message.Message
import message.Request
import message.Response
import old.TypeChannel
import java.io.IOException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

/**
 * An interface for a old.TypeChannel that handles Message data.
 */
interface MessageChannel : TypeChannel<Message>

internal val HEADER_REGEX = Regex(":\\s*")

internal val WHITESPACE_REGEX = Regex("\\s")

/**
 * Split a String into a list of Strings separated by whitespace.
 * @return List of strings.
 */
internal fun String.splitByWhitespace() = split(WHITESPACE_REGEX)

/**
 * Split a String, that is structured like an HTTP message, by it's header values.
 * @return List of strings.
 */
internal fun String.splitHeader() = split(HEADER_REGEX, 2)

/**
 * Read the next line of the request.
 * @return the next line in the buffer; will return null end of line or line did not end with a new line.
 */
@Throws(IOException::class)
internal fun ByteChannel.readLine(buffer: ByteBuffer): String? {
    val builder = StringBuilder()

    var prev = ' '
    do {
        while (buffer.hasRemaining()) {
            val current: Char = buffer.get().toChar()
            if (prev == '\r' && current == '\n') {
                return builder.substring(0, builder.length - 1)
            }
            builder.append(current)
            prev = current
        }
        buffer.clear()
    } while (read(buffer).also { buffer.flip() } != -1)

    return null
}

/**
 * Builds message into a Request or a Response. If the message cannot be built, this will throw
 * and exception
 * @throws BadMessageException when it cannot create a Request or Response object.
 * @see Response
 * @see Request
 */
@Throws(BadMessageException::class)
internal fun buildMessage(
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