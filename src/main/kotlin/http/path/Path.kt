package http.path

import http.exception.HttpException
import http.message.Request
import http.message.Response

interface Path {
    val id: String

    @Throws(HttpException::class)
    fun submit(request: Request): Response
}