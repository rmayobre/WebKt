package http.session

import http.Transaction
import http.message.Message
import http.message.Request
import http.message.channel.MessageChannel
import java.nio.channels.Channel
import java.util.*
import java.util.concurrent.TimeUnit

class HttpSession(
    channel: MessageChannel
) : Session<MessageChannel> {

    private val uuid: UUID = UUID.randomUUID()

    private val transactions: MutableSet<Transaction> = mutableSetOf()

    override val id: String
        get() = uuid.toString()

    override val created: Long = System.currentTimeMillis()

    override val channel: MessageChannel = MessageChannelWrapper(channel)

    override val history: Set<Transaction>
        get() = transactions

    override val keepAlive: Boolean
        get() = TODO("Not yet implemented")

    override fun close() {
        // TODO send message? - Should an Session be closeable?
        channel.close()
    }

    private class MessageChannelWrapper(
        private val channel: MessageChannel
    ): MessageChannel by channel {
        override fun read(): Message {
            val message = channel.read()
            // TODO - record message.
            return message
        }

        override fun read(time: Int, unit: TimeUnit): Message {
            val message = channel.read(time, unit)
            // TODO - record message.
            return message
        }

        override fun write(message: Message): Int {
            val bytesWritten = channel.write(message)
            // TODO - record message.
            return bytesWritten
        }
    }
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