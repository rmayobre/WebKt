package message

import java.lang.StringBuilder

interface Message {
    val line: String
    val headers: Map<String, String>
    val body: String?
//    val timestamp: Long TODO should a message include a timestamp of creation?
}

/**
 * Convert the headers map from Message into a String.
 * @return a String of a Message's headers.
 */
fun Message.headersToString(): String {
    val builder = StringBuilder()
    headers.forEach { (key: String, value: String) ->
        builder.append("$key : $value\n")
    }
    return builder.toString()
}