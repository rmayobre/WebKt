package http

enum class Method {
    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT;

    companion object {
        fun find(method: String): Method {
            return valueOf(method.toUpperCase())
        }
    }
}