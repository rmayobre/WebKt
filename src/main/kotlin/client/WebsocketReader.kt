package client

import frame.reader.FrameReader

internal class WebsocketReader(
    private val reader: FrameReader,
    private val handler: WebsocketEventHandler
) : Thread() {

    override fun run() {

    }

}