package websocket.server.session.factory

import http.message.Request
import websocket.frame.factory.DefaultFrameFactory
import websocket.frame.factory.FrameFactory
import websocket.frame.reader.factory.FrameBufferReaderFactory
import websocket.frame.reader.factory.FrameReaderFactory
import websocket.frame.writer.factory.FrameBufferWriterFactory
import websocket.frame.writer.factory.FrameWriterFactory
import websocket.server.session.WebsocketSession
import websocket.server.session.WebsocketSessionChannel
import java.nio.channels.SocketChannel

class WebsocketSessionChannelFactory(
    private val frameFactory: FrameFactory = DefaultFrameFactory(true),
    private val writerFactory: FrameWriterFactory = FrameBufferWriterFactory(),
    private val readerFactory: FrameReaderFactory = FrameBufferReaderFactory()
) : WebsocketSessionFactory {
    override fun create(channel: SocketChannel, request: Request): WebsocketSession {
        return WebsocketSessionChannel(
            request = request,
            channel = channel,
            factory = frameFactory,
            reader = readerFactory.create(channel),
            writer = writerFactory.create(channel)
        )
    }
}