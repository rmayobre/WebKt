package http.message

import http.Status

data class Response(
    val status: Status,
    val version: String,
    override val line: String,
    override val headers: Map<String, String>,
    override val body: String? = null
): Message {

    constructor(version: String, status: Status, headers: Map<String, String>):
            this(status, version, "$version ${status.code} ${status.message}", headers)

    data class Builder(private val status: Status) {

        private val headers = mutableMapOf<String, String>()

        private var version: String = "HTTP/1.1"

        fun setVersion(version: String) = apply { this.version = version }

        fun addHeader(key: String, value: String) = apply { headers[key] = value }

        fun build(): Response = Response(version, status, headers)
    }
}