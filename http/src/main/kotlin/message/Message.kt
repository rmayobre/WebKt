package message

interface Message {
    val line: String
    val headers: Map<String, String>
    val body: String?
//    val timestamp: Long TODO should a message include a timestamp of creation?
}