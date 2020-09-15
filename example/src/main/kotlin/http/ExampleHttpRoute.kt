package example.http

import http.content.TextType
import http.route.RestfulRoute
import http.session.Session

class ExampleHttpRoute : RestfulRoute("/") {

    private val webSocketHTML: String
        get() = loadResourceAsString(WEBSOCKET_TEST_FILE)

    override fun onGet(session: Session) {
        println("GET: ${session.request}")
        session.loadResourceAsResponse(
            resource = WEBSOCKET_TEST_FILE,
            contentType = TextType("html")
        )
    }

    companion object {
        private const val WEBSOCKET_TEST_FILE = "websocket_test.html"
    }
}