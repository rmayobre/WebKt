package http.message.writer

import http.message.Message

interface MessageWriter {
    fun write(message: Message)
}