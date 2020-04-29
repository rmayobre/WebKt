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

class WebsocketWriter(
    private val writer: FrameWriter,
    private val messageWriter: MessageWriter,
    private val messageReader: MessageReader,
    private val factory: FrameFactory,
    private val queue: BlockingQueue<Frame>,
    private val handler: WebsocketEventHandler
): Thread(), Closeable {

    var isClosed: Boolean = false
        private set

    /*
        @Throws(HandshakeException::class)
    override fun write(handshake: Handshake) {
        try {
            output.write(handshake.toByteArray())
        } catch (ex: IOException) {
            throw HandshakeException(
                "Handshake could not be complete.",
                ex
            )
        }
    }

        @Throws(WebsocketException::class)
    override fun write(handshake: Handshake) {
        try {
            channel.write(ByteBuffer.wrap(handshake.toByteArray()))
        } catch (ex: IOException) {
            throw HandshakeException(
                "Handshake could not be complete.",
                ex
            )
        }
    }
     */

    @Throws(WebsocketException::class)
    fun handshake(handshake: Handshake): Response {
        messageWriter.write(handshake)
        return messageReader.read() as Response
    }

    fun send(message: String) {
        if (!isClosed) {
            queue.put(factory.text(message))
        }
    }

    fun send(data: ByteArray) {
        if (!isClosed) {
            queue.put(factory.binary(data))
        }
    }

    fun ping(data: ByteArray? = null) {
        if (!isClosed) {
            queue.put(factory.ping(data))
        }
    }

    fun pong(data: ByteArray? = null) {
        if (!isClosed) {
            queue.put(factory.pong(data))
        }
    }

    @Synchronized
    fun close(code: ClosureCode) {
        if (!isClosed) {
            queue.put(factory.close(code))
        }
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            queue.clear()
            queue.putPoison()
        }
    }

    override fun run() {
        while (!isClosed) {
            try {
                val frame = queue.take()
                if (frame.code == OpCode.CLOSE) {
                    if (frame.length == -1) {
                        break
                    } else {
                        isClosed = true
                        writer.write(frame)
                        handler.onClose(frame.getClosureCode())
                        break
                    }
                } else {
                    writer.write(frame)
                }
            } catch (ex: WebsocketIOException) {
                handler.onError(ex)
                break
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