package example

import http.Status
import http.message.Request
import http.message.Response
import http.path.RestPath

class ExampleHttpPath(id: String) : RestPath(id) {

    constructor(): this("/")

    override fun onGet(request: Request): Response {
        println("GET: $request")
        return Response.Builder(Status.OK)
            .setBody("Successful GET request.")
            .build().also {
                println("Response: $it")
            }
    }
}