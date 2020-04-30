package client

import ClosureCode
import Handshake
import exception.WebsocketException
import exception.WebsocketIOException
import frame.Frame
import frame.OpCode
import frame.factory.FrameFactory
import frame.writer.FrameWriter
import http.message.Response
import http.message.reader.MessageReader
import http.message.writer.MessageWriter
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
            queue.putPoison()
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
        private fun BlockingQueue<Frame>.putPoison() {
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