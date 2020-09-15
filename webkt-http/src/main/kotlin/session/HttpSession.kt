package http.session

import http.message.Message
import http.message.channel.MessageChannel
import java.util.*

abstract class HttpSession(
    override val channel: MessageChannel
) : Session<Message> {

    private val uuid: UUID = UUID.randomUUID()

    private val messages: MutableSet<Message> = mutableSetOf()

    override val id: String
        get() = uuid.toString()

    override val created: Long = System.currentTimeMillis()

    override val history: Set<Message>
        get() = messages

    override val pendingRequest: Message?
        get() = TODO("Not yet implemented")

    override fun sendResponse(response: Message) {
        TODO("Not yet implemented")
    }

    fun record(message: Message): Boolean =
        messages.add(message)

//    override val channel: MessageChannel = object : MessageChannel by _channel {
//        override fun read(): Message {
//            val message = _channel.read()
//            transactions.add(message)
//            return message
//        }
//
//        override fun read(time: Int, unit: TimeUnit): Message {
//            val message = _channel.read(time, unit)
//            transactions.add(message)
//            return message
//        }
//
//        override fun write(message: Message): Int {
//            val bytesWritten = _channel.write(message)
//            transactions.add(message)
//            return bytesWritten
//        }
//    }



//    override val keepAlive: Boolean
//        get() = TODO("Not yet implemented")


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