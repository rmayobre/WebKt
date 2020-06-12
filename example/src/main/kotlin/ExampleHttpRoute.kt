package example

import http.Status
import http.message.Response
import http.route.RestfulRoute
import http.session.HttpSession

class ExampleHttpRoute : RestfulRoute("/") {

//    private val indexHTML: String
//        get() = loadWebPageAsString(HTML_TEST_FILE)

    private val webSocketHTML: String
        get() = loadWebPageAsString(WEBSOCKET_TEST_FILE)

    override fun onGet(session: HttpSession): Response {
        println("GET: ${session.request}")
        return Response.Builder(Status.OK)
            .addHeader("Content-Type", "text/html")
            .addHeader("Connection", "close")
            .setBody(webSocketHTML)
            .build()
    }

    companion object {
//        private const val HTML_TEST_FILE = "html_test.html"
        private const val WEBSOCKET_TEST_FILE = "websocket_test.html"
    }
}