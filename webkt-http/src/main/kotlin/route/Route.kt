package http.route

import http.exception.HttpException
import http.session.Session

interface Route {
    val path: String

    @Throws(HttpException::class)
    fun onRoute(session: Session)
}