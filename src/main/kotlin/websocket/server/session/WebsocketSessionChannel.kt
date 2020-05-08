package websocket.server.session

import http.message.Request
import websocket.ClosureCode
import websocket.frame.Frame
import websocket.frame.factory.FrameFactory
import websocket.frame.reader.FrameReader
import websocket.frame.writer.FrameWriter
import java.nio.channels.SocketChannel

class WebsocketSessionChannel(
    override val request: Request,
    override val channel: SocketChannel,
    private val factory: FrameFactory,
    private val reader: FrameReader,
    private val writer: FrameWriter
) : WebsocketSession {

    private var closed: Boolean = false

    override val isClosed: Boolean
        get() = closed

    override fun read(): Frame =
        reader.read(true)

    override fun send(message: String) {
        if (!isClosed) {
            writer.write(
                frame = factory.text(message)
            )
        }
    }

    override fun send(data: ByteArray) {
        if (!isClosed) {
            writer.write(
                frame = factory.binary(data)
            )
        }
    }

    override fun ping(data: ByteArray?) {
        if (!isClosed) {
            writer.write(
                frame = factory.ping(data)
            )
        }
    }

    override fun pong(data: ByteArray?) {
        if (!isClosed) {
            writer.write(
                frame = factory.pong(data)
            )
        }
    }

    @Synchronized
    override fun close(code: ClosureCode) {
        if (!isClosed) {
            closed = true
            reader.close()
            writer.write(
                frame = factory.close(code)
            )
        }
    }
}