package server

import exception.WebsocketException
import frame.OpCode
import server.session.Session
import server.session.factory.SessionFactory
import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

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
        while (isRunning) {
            if (selector.select() > 0) {
                val selectedKeys: Set<SelectionKey> = selector.selectedKeys()
                selectedKeys.forEach { key ->
                    when {
                        key.isAcceptable -> {
                            // TODO check if nonblocking is required
//                            val channel = serverSocketChannel.accept().apply {
//                                configureBlocking(false)
//                            }
                            val channel = serverSocketChannel.accept()
                            val session = factory.create(channel)
                            executor.submit(Handshake(handler, selector, session, key))
                        }

                        key.isReadable -> {
                            val session = key.attachment() as Session
                            executor.submit(ReadTransmission(handler, session))
                        }

                        key.isWritable -> {
                            val session = key.attachment() as Session
                            if (session.isWriteable) {
                                executor.submit(ReadTransmission(handler, session))
                            }
                        }
                    }
                }
            }
        }
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
     * The handshake process as a runnable. The server.SessionEventHandler
     * will decided if the server will shake hands with the Session.
     * If the server.SessionEventHandler returns true, then the server will
     * shake hands and the Websocket connection is established; otherwise
     * the server.SessionEventHandler is responsible for closing the Session.
     */
    private inner class Handshake(
        private val handler: SessionEventHandler,
        private val selector: Selector,
        private val session: Session,
        private val key: SelectionKey
    ) : Runnable {

        override fun run() {
            try {
                val doHandshake = handler.onConnection(session)
                if (doHandshake) {
                    session.handshake()
                    key.attach(session)
                    session.channel.register(selector, SelectionKey.OP_READ)
                }
            } catch (ex: WebsocketException) {
                handler.onError(session, ex)
            }
        }
    }

    private inner class ReadTransmission(
        private val handler: SessionEventHandler,
        private val session: Session
    ) : Runnable {
        override fun run() {
            try {
                val frame = session.read()
                when (frame.code) {
                    OpCode.CONTINUATION -> TODO()
                    OpCode.TEXT -> TODO()
                    OpCode.BINARY -> TODO()
                    OpCode.CLOSE -> TODO()
                    OpCode.PING -> TODO()
                    OpCode.PONG -> TODO()
                }
            } catch (ex: WebsocketException) {
                handler.onError(session, ex)
            }
        }
    }

    private inner class WriteTransmission(
        private val handler: SessionEventHandler,
        private val session: Session
    ) : Runnable {
        override fun run() {
            try {
                session.write()
            } catch (ex: WebsocketException) {
                handler.onError(session, ex)
            }
        }
    }

    companion object {
        /** The executor will wait 60 seconds for it's tasks to finish before termination. */
        private const val TERMINATION_TIMEOUT_SECONDS = 60L
        private const val CHANNELER_THREAD_NAME = "Websocket-Channeler-Thread"
    }
}