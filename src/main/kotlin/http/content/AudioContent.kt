package http.content

class AudioContent(
    override val type: String
): Content {
    override val mime: String = "audio"
}