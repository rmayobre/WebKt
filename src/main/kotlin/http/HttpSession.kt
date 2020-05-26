package http

import http.message.Request
import java.nio.channels.SocketChannel

class HttpSession(
    private val request: Request,
    private val channel: SocketChannel
) {

    val keepAlive: Boolean
        get() = request.headers[CONNECTION_HEADER] == CONNECTION_KEEP_ALIVE


    companion object {

        /*
         * Connection header and values.
         */
        private const val CONNECTION_HEADER = "Connection"
        private const val CONNECTION_KEEP_ALIVE = "keep-alive"
        private const val CONNECTION_CLOSE = "close"
    }
}