package http.message

interface Message {
    val line: String
    val headers: Map<String, String>
    val body: String?
}