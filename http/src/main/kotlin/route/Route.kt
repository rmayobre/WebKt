package http.route

import http.exception.HttpException
import http.message.Message
import http.session.HttpSession

interface Route {
    val path: String

    @Throws(HttpException::class)
    fun onRoute(session: HttpSession): Message
}