package http.content

class TextContent(
    override val type: String
): Content {
    override val mime: String = "text"
}