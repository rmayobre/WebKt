package http.content

class ApplicationContent(
    override val type: String
): Content {
    override val mime: String = "application"
}