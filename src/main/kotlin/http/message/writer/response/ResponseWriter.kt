package http.message.writer.response

import http.message.Response

interface ResponseWriter {
    fun write(response: Response)
}