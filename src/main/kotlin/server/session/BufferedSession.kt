package server.session

import ClosureCode
import exception.HandshakeException
import frame.Frame
import frame.factory.FrameFactory
import frame.reader.FrameReader
import frame.writer.FrameWriter
import http.Request
import java.nio.channels.SocketChannel
import java.util.*

class BufferedSession(
    override val request: Request,
    override val channel: SocketChannel,
    private val factory: FrameFactory,
    private val reader: FrameReader,
    private val writer: FrameWriter
) : Session {

    private val frameQueue: Queue<Frame> = LinkedList()

    private var _isClosed: Boolean = false

    override val isClosed: Boolean
        get() = _isClosed

    override val isWriteable: Boolean
        get() = frameQueue.isNotEmpty()

    override fun read(): Frame =
        reader.read(true)

    override fun write() {
        frameQueue.poll()?.let {
            writer.write(it)
        }
    }

    override fun handshake(headers: Map<String, String>?) {
        request.webSocketKey?.let { key ->
            val handshake = Handshake.Server(key).apply {
                headers?.let {
                    it.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
            }.build()
            writer.write(handshake)
        } ?: throw HandshakeException("Request did not provided a key.")
    }

    override fun send(message: String) {
        frameQueue.add(factory.text(message))
    }

    override fun send(data: ByteArray) {
        frameQueue.add(factory.binary(data))
    }

    override fun ping(data: ByteArray?) {
        frameQueue.add(factory.ping(data))
    }

    override fun pong(data: ByteArray?) {
        frameQueue.add(factory.pong(data))
    }

    override fun close(code: ClosureCode) {
        _isClosed = true
        reader.close()
        frameQueue.clear()
        frameQueue.add(factory.close(code))
    }

}