package http.exception

import http.message.Response
import kotlin.reflect.KClass

abstract class HttpExceptionHandler<T : HttpException>(val type: KClass<T>) {
    abstract fun handle(exception: T): Response
}