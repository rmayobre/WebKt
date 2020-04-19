import http.Method
import java.net.URI

data class Handshake(
    val uri: URI,
    val path: String,
    val method: Method
) {
    fun toByteArray(): ByteArray {
        TODO("Build ByteArray.")
    }
}