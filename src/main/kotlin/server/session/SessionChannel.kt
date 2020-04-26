package server.session

import ClosureCode
import exception.HandshakeException
import frame.Frame
import frame.factory.FrameFactory
import frame.reader.FrameReader
import frame.writer.FrameWriter
import http.message.Request
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService

class SessionChannel(
    override val request: Request,
    override val channel: SocketChannel,
    private val executor: ExecutorService,
    private val factory: FrameFactory,
    private val reader: FrameReader,
    private val writer: FrameWriter
) : Session {

    private var _isClosed: Boolean = false

    override val isClosed: Boolean
        get() = _isClosed

    override fun read(): Frame =
        reader.read(true)

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
        if (!isClosed) {
            executor.submit {
                writer.write(
                    frame = factory.text(message)
                )
            }
        }
    }

    override fun send(data: ByteArray) {
        if (!isClosed) {
            executor.submit {
                writer.write(
                    frame = factory.binary(data)
                )
            }
        }
    }

    override fun ping(data: ByteArray?) {
        if (!isClosed) {
            executor.submit {
                writer.write(
                    frame = factory.ping(data)
                )
            }
        }
    }

    override fun pong(data: ByteArray?) {
        if (!isClosed) {
            executor.submit {
                writer.write(
                    frame = factory.pong(data)
                )
            }
        }
    }

    override fun close(code: ClosureCode) {
        if (!isClosed) {
            _isClosed = true
            reader.close()
            executor.submit {
                writer.write(
                    frame = factory.close(code)
                )
            }
        }
    }

}