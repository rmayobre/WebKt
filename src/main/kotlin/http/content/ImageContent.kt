package http.content

class ImageContent(
    override val type: String
): Content {
    override val mime: String = "image"
}