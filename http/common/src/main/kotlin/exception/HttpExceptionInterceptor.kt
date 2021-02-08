package exception

import message.Response
import kotlin.reflect.KClass

abstract class HttpExceptionInterceptor<T : HttpException>(val type: KClass<T>) {
    abstract fun onException(exception: T): Response
}