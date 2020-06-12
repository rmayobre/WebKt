package http.session

import http.message.Request
import java.io.Closeable
import java.nio.channels.Channel

interface HttpSession : Closeable {
    val channel: Channel
    val request: Request
    val keepAlive: Boolean
    val isUpgrade: Boolean
}