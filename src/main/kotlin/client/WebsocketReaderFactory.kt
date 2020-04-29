package client

interface WebsocketReaderFactory {
    fun create(): WebsocketReader
}