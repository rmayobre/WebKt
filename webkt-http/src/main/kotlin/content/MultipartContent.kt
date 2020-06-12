package http.content

class MultipartContent(
    override val type: String
): Content {
    override val mime: String = "multipart"
}