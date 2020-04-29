package client

import ClosureCode
import exception.InvalidFrameException
import exception.WebsocketException
import exception.WebsocketIOException
import frame.Frame
import frame.OpCode
import frame.reader.FrameReader
import java.io.Closeable
import java.nio.ByteBuffer

class WebsocketReader(
    private val reader: FrameReader,
    private val handler: WebsocketEventHandler
) : Thread(), Closeable {

    var isClosed: Boolean = false
        private set

    init { start() }

    override fun run() {
        while (!isClosed) {
            try {
                val frame = reader.read(false)
                when (frame.code) {
                    OpCode.TEXT -> handler.onMessage(frame.getMessage())
                    OpCode.BINARY -> handler.onMessage(frame.data)
                    OpCode.CLOSE -> handler.onClose(frame.getClosureCode())
                    OpCode.PING -> handler.onPing(frame.data)
                    OpCode.PONG -> handler.onPong(frame.data)
                    OpCode.CONTINUATION -> handler.onError(InvalidFrameException(
                        "A continuation frame was sent. Continuation frames must" +
                                " be fragmented from text or binary frames."))
                }
            } catch (ex: WebsocketIOException) {
                // If reader isn't closed, close it and send an error.
                // Always break loop when this exception occurs.
                if (!isClosed) {
                    isClosed = true
                    handler.onError(ex)
                }
                break
            } catch (ex: WebsocketException) {
                handler.onError(ex)
            }
        }
    }

    @Synchronized
    override fun close() {
        if (!isClosed) {
            isClosed = true
            reader.close()
        }
    }

   companion object {
       private fun Frame.getMessage(): String {
           return data.toString(Charsets.UTF_8)
       }

       private fun Frame.getClosureCode(): ClosureCode {
           return ClosureCode.find(ByteBuffer.wrap(data).int)
       }
   }
}