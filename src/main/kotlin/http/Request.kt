package http

import java.io.BufferedReader
import java.io.IOException
import java.net.URI

data class Request(
    val uri: URI,
    val path: String,
    val method: Method,
    private val headers: Map<String, String>
) {
    val isWebSocketUpgrade: Boolean
        get() = headers["Upgrade"] == "Websocket"
                && headers["Connection"] == "Upgrade"
                && headers["Sec-WebSocket-Version"] == "13"

    val webSocketKey: String? = headers["Sec-WebSocket-Key"]

    fun getHeader(key: String): String? = headers[key]

    companion object {

        // TODO move into FrameInputStreamReader
        @Throws(IOException::class)
        fun create(reader: BufferedReader): Request {
            val requestLine: List<String> = reader.readLine().split("\\s+")
            val headers: MutableMap<String, String> = hashMapOf()
            var header: String = reader.readLine()
            while(header.isNotEmpty()) {
                val h: List<String> = header.split(":\\s+", limit = 2)
                headers[h[0]] = h[1]
                header = reader.readLine()
            }

            return Request(
                method = Method.find(requestLine[0]),
                uri = URI(requestLine[1]),
                path = requestLine[1].substring(0, requestLine[1].lastIndexOf("/")+1),
                headers = headers
            )
        }

        // TODO move into FrameBufferReader
        @Throws(IllegalArgumentException::class)
        fun create(request: ByteArray): Request = create(String(request))

        @Throws(IllegalArgumentException::class)
        fun create(request: String): Request = create(request.split("\\r?\\n"))

        @Throws(IllegalArgumentException::class)
        fun create(lines: List<String>): Request {
            val requestLines: List<String> = lines[0].split("\\s+")
            val headers = mutableMapOf<String, String>()
            for (i in 1 until lines.size) {
                val header: List<String> = lines[i].split(Regex(":\\s+"), 2)
                headers[header[0]] = header[1]
            }

            return Request(
                uri = URI(requestLines[1]),
                path = requestLines[1].substring(0, requestLines[1].lastIndexOf("/") + 1),
                method = Method.valueOf(requestLines[0].toUpperCase()),
                headers = mutableMapOf<String, String>().apply {
                    for (i in 1 until lines.size) {
                        val header: List<String> = lines[i].split(Regex(":\\s+"), 2)
                        put(header[0], header[1])
                    }
                })
        }
    }
}