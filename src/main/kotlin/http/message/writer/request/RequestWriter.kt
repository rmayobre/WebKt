package http.message.writer.request

import http.message.Request
import http.message.Response

interface RequestWriter {
    fun write(request: Request): Response
}