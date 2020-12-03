package session

import message.Message
import message.channel.MessageChannel
import TypeChannel
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

open class HttpSession(
    override val id: String,
    override val channel: MessageChannel,
    override val sslSession: SSLSession? = null
) : Session<Message> {

//    private val uuid: UUID = UUID.randomUUID()

    private val messages: MutableSet<Message> = mutableSetOf()

//    override val id: String
//        get() = uuid.toString()

    override val created: Long = System.currentTimeMillis()

    override val history: Set<Message>
        get() = messages // TODO provide a copy, not the real list of transactions.

    override var pending: Message? = null
}

//(
//    override val channel: Channel,
//    override val request: Request,
//    override var response: Message // place a default response here.
//) : Session {
//
//    override val keepAlive: Boolean
//        get() = (request.headers[CONNECTION_HEADER] == CONNECTION_KEEP_ALIVE ||
//            request.headers[CONNECTION_HEADER] == CONNECTION_UPGRADE ||
//            request.headers[CONNECTION_HEADER] == CONNECTION_KEEP_ALIVE_UPGRADE) &&
//            (response.headers[CONNECTION_HEADER] == CONNECTION_KEEP_ALIVE ||
//                response.headers[CONNECTION_HEADER] == CONNECTION_UPGRADE)
//
//    override val isUpgrade: Boolean
//        get() = request.headers[CONNECTION_HEADER] == CONNECTION_UPGRADE &&
//            response.headers[CONNECTION_HEADER] == CONNECTION_UPGRADE
//
//    override fun close() {
//        if (channel.isOpen) {
//            channel.close()
//        }
//    }
//
//    companion object {
//
//        /*
//         * Connection header and values.
//         */
//        private const val CONNECTION_HEADER = "Connection"
//        private const val CONNECTION_KEEP_ALIVE = "keep-alive"
//        private const val CONNECTION_KEEP_ALIVE_UPGRADE = "keep-alive, Upgrade"
//        private const val CONNECTION_CLOSE = "close"
//        private const val CONNECTION_UPGRADE = "Upgrade"
//    }
//}