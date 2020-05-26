package http.path

import http.Method.*
import http.exception.BadRequestException
import http.exception.HttpException
import http.message.Request
import http.message.Response
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

open class RestPath(override val id: String) : Path {

    @Throws(HttpException::class)
    override fun onRequest(channel: SocketChannel, request: Request): Response = when (request.method) {
        GET -> onGet(request)
        PUT -> onPut(request)
        POST -> onPost(request)
        DELETE -> onDelete(request)
        HEAD -> onHead(request)
        OPTIONS -> onOptions(request)
        TRACE -> onTrace(request)
        CONNECT -> onConnect(request)
    }

    @Throws(HttpException::class)
    protected open fun onGet(request: Request): Response {
        throw BadRequestException("This path does not support GET method.")
    }

    @Throws(HttpException::class)
    protected open fun onPut(request: Request): Response {
        throw BadRequestException("This path does not support PUT method.")
    }

    @Throws(HttpException::class)
    protected open fun onPost(request: Request): Response {
        throw BadRequestException("This path does not support POST method.")
    }

    @Throws(HttpException::class)
    protected open fun onDelete(request: Request): Response {
        throw BadRequestException("This path does not support DELETE method.")
    }

    @Throws(HttpException::class)
    protected open fun onHead(request: Request): Response {
        throw BadRequestException("This path does not support HEAD method.")
    }

    @Throws(HttpException::class)
    protected open fun onOptions(request: Request): Response {
        throw BadRequestException("This path does not support OPTIONS method.")
    }

    @Throws(HttpException::class)
    protected open fun onTrace(request: Request): Response {
        throw BadRequestException("This path does not support TRACE method.")
    }

    @Throws(HttpException::class)
    protected open fun onConnect(request: Request): Response {
        throw BadRequestException("This path does not support CONNECT method.")
    }
}