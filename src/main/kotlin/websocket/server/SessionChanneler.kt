package websocket.server

import websocket.ClosureCode
import websocket.InvalidFrameException
import websocket.NoUTFException
import websocket.WebsocketException
import websocket.frame.Frame
import websocket.frame.OpCode
import websocket.server.session.Session
import websocket.server.session.factory.SessionFactory
import java.io.Closeable
import java.io.UnsupportedEncodingException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

// TODO make into an engine design.
@Deprecated("remove and replace with WebEngine")
class SessionChanneler(
    private val factory: SessionFactory,
    private val executor: ExecutorService,
    private val address: InetSocketAddress,
    private val handler: SessionEventHandler
) : Thread(CHANNELER_THREAD_NAME), Closeable {

    private lateinit var serverSocketChannel: ServerSocketChannel

    private lateinit var selector: Selector

    private var isRunning: Boolean = false

    override fun start() {
        isRunning = true
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open().apply {
            bind(address)
            configureBlocking(false)
            register(selector, SelectionKey.OP_ACCEPT)
        }
        super.start()
    }

    override fun run() {
        println("Channeler is now running...")
        while (isRunning) {
            if (selector.select() > 0) {
                val selectedKeys: Set<SelectionKey> = selector.selectedKeys()
                selectedKeys.forEach { key ->
                    key.interestOps()
                    if (key.isValid) {
                        when {
                            key.isAcceptable -> {
                                serverSocketChannel.accept()?.apply {
                                    configureBlocking(false)
                                    socket().apply {
                                        tcpNoDelay = false
                                        keepAlive = true
                                    }
                                    register(selector, SelectionKey.OP_READ or SelectionKey.OP_WRITE)
                                }
                            }

                            key.isReadable -> {
                                if (key.attachment() == null) {
                                    val channel: SocketChannel = key.channel() as SocketChannel
                                    executor.submit(Handshake(channel, key))
                                } else {
                                    val session: Session = key.attachment() as Session
                                    executor.submit(ReadTransmission(session))
                                }
                            }
                        }
                    }
                }
            }
        }
        println("No longer running...")
    }



    override fun close() {
        isRunning = false
        try {
            executor.shutdown()
            executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (ex: InterruptedException) {
            executor.shutdownNow()
        }
        selector.close()
        serverSocketChannel.close()
    }

    /**
     * The handshake process as a runnable. The websocket.server.SessionEventHandler
     * will decided if the websocket.server will shake hands with the http.Session.
     * If the websocket.server.SessionEventHandler returns true, then the websocket.server will
     * shake hands and the Websocket connection is established; otherwise
     * the websocket.server.SessionEventHandler is responsible for closing the http.Session.
     */
    private inner class Handshake(
        private val channel: SocketChannel,
        private val key: SelectionKey
    ) : Runnable {

        override fun run() {
            try {
                val session = factory.create(channel, executor)
                try {
                    key.attach(session)
                    handler.onConnection(session)
                } catch (ex: WebsocketException) {
                    handler.onError(session, ex)
                }
            } catch (ex: Exception) {
                handler.onError(ex)
                channel.close()
                key.cancel()
            }
        }
    }

    private inner class ReadTransmission(
        private val session: Session
    ) : Runnable {
        override fun run() {
            try {
                val frame = session.read()
                when (frame.code) {
                    OpCode.TEXT -> handleText(frame)
                    OpCode.BINARY -> handleBinary(frame)
                    OpCode.CLOSE -> handleClose(frame)
                    OpCode.PING -> handlePing(frame)
                    OpCode.PONG -> handlePong(frame)
                    else -> throw InvalidFrameException(
                        "Frame's OpCode broke RFC 6455 policy, cannot " +
                                "send a continuation websocket.frame that is not attached" +
                                " to a data websocket.frame."
                    )
                }
            } catch (ex: WebsocketException) {
                handler.onError(session, ex)
            } catch (ex: Exception) {
                handler.onError(ex)
            }
        }

        @Throws(WebsocketException::class)
        private fun handleText(frame: Frame) {
            try {
                val message = frame.data.toString(Charsets.UTF_8)
                handler.onMessage(session, message)
            } catch (ex: UnsupportedEncodingException) {
                throw NoUTFException(ex)
            }
        }

        @Throws(WebsocketException::class)
        private fun handleBinary(frame: Frame) {
            handler.onMessage(session, frame.data)
        }

        @Throws(WebsocketException::class)
        private fun handleClose(frame: Frame) {
            if (frame.length == 0) {
                handler.onClose(session, ClosureCode.NO_STATUS)
            } else {
                // TODO Buffer under flow exception thrown here on page refresh.
                if (frame.data.isEmpty()) {
                    handler.onClose(session, ClosureCode.NO_STATUS)
                } else {
                    val code = ClosureCode.find(ByteBuffer.wrap(frame.data).int)
                    handler.onClose(session, code)
                }
            }
        }

        @Throws(WebsocketException::class)
        private fun handlePing(frame: Frame) {
            handler.onPing(session, frame.data)
        }

        @Throws(WebsocketException::class)
        private fun handlePong(frame: Frame) {
            handler.onPong(session, frame.data)
        }
    }

//    private inner class WriteTransmission(
//        private val handler: SessionEventHandler,
//        private val session: http.Session
//    ) : Runnable {
//        override fun run() {
//            try {
//                session.write()
//            } catch (ex: WebsocketException) {
//                handler.onError(session, ex)
//            }
//        }
//    }

    companion object {
        /** The executor will wait 60 seconds for it's tasks to finish before termination. */
        private const val TERMINATION_TIMEOUT_SECONDS = 60L
        private const val CHANNELER_THREAD_NAME = "Websocket-Channeler-Thread"
    }
}