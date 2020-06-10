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
            .addHeader("Content-Type", "text/html")
            .setBody("<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>Page Title</title>" +
                "</head>" +
                "<body>" +
                "" +
                "<h1>This is a Heading</h1>" +
                "<p>This is a paragraph.</p>" +
                "" +
                "</body>" +
                "</html>")
//            .setBody("<!DOCTYPE html>\n" +
//                "<html lang=\"en\">\n" +
//                "<head>\n" +
//                "    <meta charset=\"UTF-8\">\n" +
//                "    <title>Hello World!</title>\n" +
//                "</head>\n" +
//                "<body>\n" +
//                "\n" +
//                "</body>\n" +
//                "</html>")
            .build().also {
                println("Response: $it")
            }
//        return Response.Builder(Status.OK)
//            .setBody("Successful GET request.")
//            .build().also {
//                println("Response: $it")
//            }
    }
}