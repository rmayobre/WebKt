package client

import ClosureCode
import exception.WebsocketException

interface WebsocketEventHandler {
    fun onOpen(/*response: HandshakeData*/)
    fun onMessage(message: String)
    fun onMessage(data: ByteArray)
    fun onPing(data: ByteArray)
    fun onPong(data: ByteArray)
    fun onClose(closureCode: ClosureCode)
    fun onError(exception: WebsocketException)
}