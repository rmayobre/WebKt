package client

interface WebsocketWriterFactory {
    fun create(): WebsocketWriter
}