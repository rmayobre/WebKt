package client

import ClosureCode
import exception.WebsocketException
//import HandshakeData

interface WebsocketListener {
    fun onOpen(websocket: Websocket/*response: HandshakeData*/)
    fun onMessage(websocket: Websocket, message: String)
    fun onMessage(websocket: Websocket, data: ByteArray)
    fun onPing(websocket: Websocket, data: ByteArray)
    fun onPong(websocket: Websocket, data: ByteArray)
    fun onClose(websocket: Websocket, closureCode: ClosureCode)
    fun onError(websocket: Websocket, exception: WebsocketException)
}