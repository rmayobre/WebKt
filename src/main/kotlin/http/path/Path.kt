package http.path

import http.exception.HttpException
import http.message.Message
import http.message.Request
import http.message.Response
import java.nio.channels.SelectionKey

interface Path {
    val id: String

    @Throws(HttpException::class)
    fun onRequest(key: SelectionKey, request: Request): Message
}