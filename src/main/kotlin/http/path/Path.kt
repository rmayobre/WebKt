package http.path

import http.exception.HttpException
import http.message.Message
import http.message.Request
import http.message.Response
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

interface Path {
    val id: String

    @Throws(HttpException::class)
    fun onRequest(channel: SocketChannel, request: Request): Message
}