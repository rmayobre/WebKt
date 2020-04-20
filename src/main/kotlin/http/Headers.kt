package http


data class Headers(
    val path: String,
    val method: Method,
    val version: String,
    private val headers: Map<String, String>
) {
    fun getHeader(key: String): String? = headers[key]
}