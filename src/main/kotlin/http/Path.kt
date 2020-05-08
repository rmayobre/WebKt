package http

import http.Method.*
import http.exception.BadRequestException
import http.exception.HttpException
import http.message.Request
import http.message.Response

open class Path(val key: String) {

    @Throws(HttpException::class)
    fun submit(request: Request): Response = when (request.method) {
        GET -> get(request)
        PUT -> put(request)
        POST -> post(request)
        DELETE -> delete(request)
        HEAD -> head(request)
        OPTIONS -> options(request)
        TRACE -> trace(request)
        CONNECT -> connect(request)
    }

    @Throws(HttpException::class)
    protected fun get(request: Request): Response {
        throw BadRequestException("This path does not support GET method.")
    }

    @Throws(HttpException::class)
    protected fun put(request: Request): Response {
        throw BadRequestException("This path does not support PUT method.")
    }

    @Throws(HttpException::class)
    protected fun post(request: Request): Response {
        throw BadRequestException("This path does not support POST method.")
    }

    @Throws(HttpException::class)
    protected fun delete(request: Request): Response {
        throw BadRequestException("This path does not support DELETE method.")
    }

    @Throws(HttpException::class)
    protected fun head(request: Request): Response {
        throw BadRequestException("This path does not support HEAD method.")
    }

    @Throws(HttpException::class)
    protected fun options(request: Request): Response {
        throw BadRequestException("This path does not support OPTIONS method.")
    }

    @Throws(HttpException::class)
    protected fun trace(request: Request): Response {
        throw BadRequestException("This path does not support TRACE method.")
    }

    @Throws(HttpException::class)
    protected fun connect(request: Request): Response {
        throw BadRequestException("This path does not support CONNECT method.")
    }
}