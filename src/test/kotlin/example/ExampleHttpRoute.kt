package example

import http.Status
import http.message.Request
import http.message.Response
import http.route.RestfulRoute
import http.session.HttpSession

class ExampleHttpRoute(path: String = "/") : RestfulRoute(path) {
    override fun onGet(session: HttpSession): Response {
        println("GET: ${session.request}")
        session.keepAlive = false
        return Response.Builder(Status.OK)
            .setBody("Successful GET request.")
            .build().also {
                println("Response: $it")
            }
    }
}