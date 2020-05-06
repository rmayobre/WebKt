package http.message.reader

import http.message.Message
import java.util.concurrent.TimeUnit

class MessageBufferReader : MessageReader {
    override fun read(): Message {
        TODO("Not yet implemented")
    }

    override fun read(time: Int, unit: TimeUnit): Message {
        TODO("Not yet implemented")
    }
}