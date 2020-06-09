package http.route

import http.Method
import http.exception.BadRequestException
import http.exception.HttpException
import http.message.Message
import http.message.Response
import http.session.HttpSession

open class RestfulRoute(override val path: String) : Route {

    override fun onRoute(session: HttpSession): Message = when (session.request.method) {
        Method.GET -> onGet(session)
        Method.PUT -> onPut(session)
        Method.POST -> onPost(session)
        Method.DELETE -> onDelete(session)
        Method.HEAD -> onHead(session)
        Method.OPTIONS -> onOptions(session)
        Method.TRACE -> onTrace(session)
        Method.CONNECT -> onConnect(session)
    }

    @Throws(HttpException::class)
    protected open fun onGet(session: HttpSession): Response {
        throw BadRequestException("This path does not support GET method.")
    }

    @Throws(HttpException::class)
    protected open fun onPut(session: HttpSession): Response {
        throw BadRequestException("This path does not support PUT method.")
    }

    @Throws(HttpException::class)
    protected open fun onPost(session: HttpSession): Response {
        throw BadRequestException("This path does not support POST method.")
    }

    @Throws(HttpException::class)
    protected open fun onDelete(session: HttpSession): Response {
        throw BadRequestException("This path does not support DELETE method.")
    }

    @Throws(HttpException::class)
    protected open fun onHead(session: HttpSession): Response {
        throw BadRequestException("This path does not support HEAD method.")
    }

    @Throws(HttpException::class)
    protected open fun onOptions(session: HttpSession): Response {
        throw BadRequestException("This path does not support OPTIONS method.")
    }

    @Throws(HttpException::class)
    protected open fun onTrace(session: HttpSession): Response {
        throw BadRequestException("This path does not support TRACE method.")
    }

    @Throws(HttpException::class)
    protected open fun onConnect(session: HttpSession): Response {
        throw BadRequestException("This path does not support CONNECT method.")
    }
}