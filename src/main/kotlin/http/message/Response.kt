package http.message

import http.Status

data class Response(
    val status: Status,
    val version: String,
    override val line: String,
    override val headers: Map<String, String>,
    override val body: String? = null
): Message {

    data class Builder(private val status: Status) {

        private val headers = mutableMapOf<String, String>()

        private var version: String = "HTTP/1.1"

        private var body: String? = null

        private val statusLine: String
            get() = "$version ${status.code} ${status.message}"

        fun setVersion(version: String) = apply { this.version = version }

        fun addHeader(key: String, value: String) = apply { headers[key] = value }

        fun setBody(body: String) = apply { this.body = body }

        fun build(): Response = Response(status, version, statusLine, headers, body)
    }
}