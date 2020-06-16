package example

import http.exception.HttpException
import http.exception.HttpExceptionInterceptor
import http.message.Response

class ExampleHttpExceptionInterceptor : HttpExceptionInterceptor<HttpException>(HttpException::class) {
    override fun onException(exception: HttpException): Response {
        println("HttpException (Message): ${exception.message}")
        println("HttpException (Reason): ${exception.reason}")
        println("HttpException (Response): ${exception.response}")
        println("HttpException (Body): ${exception.body}")
        return exception.response
    }
}