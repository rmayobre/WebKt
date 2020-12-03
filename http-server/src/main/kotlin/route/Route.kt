package route

import http.exception.HttpException
import session.Session

interface Route {
    val path: String

    @Throws(HttpException::class)
    fun onRoute(session: Session)
}