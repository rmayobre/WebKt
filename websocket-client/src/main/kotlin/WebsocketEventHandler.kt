import websocket.ClosureCode
import websocket.WebsocketException

interface WebsocketEventHandler {
    fun onOpen()
    fun onMessage(message: String)
    fun onMessage(data: ByteArray)
    fun onPing(data: ByteArray)
    fun onPong(data: ByteArray)
    fun onClose(closureCode: ClosureCode)
    fun onError(exception: WebsocketException)
}