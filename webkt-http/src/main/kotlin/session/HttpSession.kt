package http.session

import http.message.Message
import http.message.Request
import java.io.Closeable
import java.nio.channels.Channel

interface HttpSession : Closeable {
    val channel: Channel
    var request: Message?
    var response: Message?
    val keepAlive: Boolean
    val isUpgrade: Boolean
    // TODO add a write function.

}