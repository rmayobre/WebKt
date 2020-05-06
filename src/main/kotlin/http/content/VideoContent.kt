package http.content

class VideoContent(
    override val type: String
): Content {
    override val mime: String = "video"
}