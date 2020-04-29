package http.message.writer.request

import http.message.Request
import http.message.Response
import java.io.IOException

interface RequestWriter {
    @Throws(IOException::class)
    fun write(request: Request): Response
}