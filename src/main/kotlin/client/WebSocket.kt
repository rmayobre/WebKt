package client

import ClosureCode
import java.io.Closeable

interface WebSocket : Closeable {
    //TODO turn into a Class. Create a list of listeners. Make this implement the listener
    // Have the user override the callbacks of the WebSocketListener or create
    // new listeners to handle the callbacks.


    fun send(message: String)
    fun send(data: ByteArray)
    fun ping(data: ByteArray? = null)
    fun close(code: ClosureCode? = null)

}