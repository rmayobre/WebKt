package http.message.reader

import http.message.Message

interface MessageReader {
    fun read(): Message
}