package http.message.channel

import http.message.BadMessageException
import http.message.Message
import http.message.buildMessage
import http.message.splitHeader
import java.io.*
import java.lang.StringBuilder
import java.nio.channels.Channel
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class MessageStreamChannel 

@Throws(IOException::class) 
constructor(private val channel: SocketChannel) : MessageChannel, Channel by channel {
    
    private val input: InputStream = channel.socket().getInputStream()
    
    private val output: OutputStream = channel.socket().getOutputStream()

    @Throws(IOException::class, BadMessageException::class)
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

    @Throws(IOException::class,
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