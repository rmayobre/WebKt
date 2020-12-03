import websocket.ClosureCode
import websocket.WebsocketException
import websocket.WebsocketIOException
import websocket.frame.Frame
import websocket.frame.OpCode
import websocket.frame.factory.FrameFactory
import websocket.frame.writer.FrameWriter
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue

// TODO Test writer's stability and closing process.
class WebsocketWriter(
    private val writer: FrameWriter,
    private val factory: FrameFactory,
    private val queue: BlockingQueue<Frame>,
    private val handler: WebsocketEventHandler
): Thread(), Closeable {

    fun send(message: String) {
        if (isAlive) {
            queue.put(factory.text(message))
        }
    }

    fun send(data: ByteArray) {
        if (isAlive) {
            queue.put(factory.binary(data))
        }
    }

    fun ping(data: ByteArray? = null) {
        if (isAlive) {
            queue.put(factory.ping(data))
        }
    }

    fun pong(data: ByteArray? = null) {
        if (isAlive) {
            queue.put(factory.pong(data))
        }
    }

    @Synchronized
    fun close(code: ClosureCode) {
        if (isAlive) {
            queue.put(factory.close(code))
        }
    }

    override fun close() {
        if (isAlive) {
            queue.clear()
            queue.stop()
        }
    }

    override fun run() {
        while (true) {
            try {
                val frame = queue.take()
                if (frame.code == OpCode.CLOSE) {
                    if (frame.length == -1) {
                        return
                    } else {
                        writer.write(frame)
                        handler.onClose(frame.getClosureCode())
                        return
                    }
                } else {
                    writer.write(frame)
                }
            } catch (ex: WebsocketIOException) {
                handler.onError(ex)
                return
            } catch (ex: WebsocketException) {
                handler.onError(ex)
            }
        }
    }

    companion object {
        /** Acts as the poison pill to stop writer. */
        private fun BlockingQueue<Frame>.stop() {
            put(Frame(
                isFin = true,
                rsv1 = false,
                rsv2 = false,
                rsv3 = false,
                isMasked = false,
                code = OpCode.CLOSE,
                length = -1,
                payload = ByteArrayOutputStream()
            ))
        }

        private fun Frame.getClosureCode(): ClosureCode {
            return ClosureCode.find(ByteBuffer.wrap(data).int)
        }
    }
}