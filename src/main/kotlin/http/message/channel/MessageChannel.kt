package http.message.channel

import http.message.BadMessageException
import http.message.Message
import java.io.IOException
import java.nio.channels.Channel
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

interface MessageChannel : Channel {
    @Throws(IOException::class,
            BadMessageException::class)
    fun read(): Message

    @Throws(IOException::class,
            TimeoutException::class,
            BadMessageException::class)
    fun read(time: Int, unit: TimeUnit): Message

    @Throws(IOException::class)
    fun write(message: Message)
}