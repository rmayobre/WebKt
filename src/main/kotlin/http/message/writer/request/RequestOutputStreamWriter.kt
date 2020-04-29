package http.message.writer.request

import http.message.Request
import http.message.Response
import http.message.reader.MessageReader
import http.message.writer.MessageWriter

class RequestOutputStreamWriter(
    private val reader: MessageReader,
    private val writer: MessageWriter
) : RequestWriter {
    override fun write(request: Request): Response {
        writer.write(request)


        TODO("Not yet implemented")
    }
}