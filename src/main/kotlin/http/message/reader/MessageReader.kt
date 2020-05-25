package http.message.reader

import http.message.BadMessageException
import http.message.Message
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Deprecated("Replaced with Message Channels")
interface MessageReader {

    @Throws(
        IOException::class,
        BadMessageException::class)
    fun read(): Message

    @Throws(
        IOException::class,
        TimeoutException::class,
        BadMessageException::class)
    fun read(time: Int, unit: TimeUnit): Message
}