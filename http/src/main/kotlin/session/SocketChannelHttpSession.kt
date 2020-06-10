package http.session

import http.message.Request
import java.nio.channels.Channel

class SocketChannelHttpSession(
    override val channel: Channel,
    override val request: Request
) : HttpSession {

    override var keepAlive: Boolean = request.headers[CONNECTION_HEADER] == CONNECTION_KEEP_ALIVE

    override fun close() {
        if (channel.isOpen) {
            channel.close()
        }
    }

    companion object {

        /*
         * Connection header and values.
         */
        private const val CONNECTION_HEADER = "Connection"
        private const val CONNECTION_KEEP_ALIVE = "keep-alive"
        private const val CONNECTION_CLOSE = "close"
    }
}