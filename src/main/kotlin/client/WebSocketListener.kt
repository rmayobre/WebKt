package client

import ClosureCode
import exception.WebsocketException

interface WebSocketListener {
    fun onOpen()
    fun onMessage(message: String)
    fun onMessage(data: ByteArray)
    fun onPing()
    fun onPong()
    fun onClose(closureCode: ClosureCode)
    fun onError(ex: WebsocketException)
}