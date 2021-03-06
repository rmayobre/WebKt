package http

import ServerSocketChannelEngine
import http.exception.BadRequestException
import http.exception.ExceptionHandler
import http.exception.HttpException
import http.exception.HttpExceptionInterceptor
import http.message.Message
import http.message.Response
import http.message.channel.MessageChannel
import http.message.channel.factory.MessageBufferChannelFactory
import http.message.channel.factory.MessageChannelFactory
import http.route.Route
import http.route.RunnableRoute
import http.session.HttpSession
import http.session.factory.HttpSessionFactory
import http.session.factory.SocketChannelHttpSessionFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

/**
 * HTTP protocol layer of the ServerSocketChannelEngine inheritance.
 */
open class HttpEngine protected constructor(
    private val factory: MessageChannelFactory,
    private val sessionFactory: HttpSessionFactory<SocketChannel>,
    private val httpExceptionHandlers: Map<KClass<*>, HttpExceptionInterceptor<*>>,
    private val exceptionHandlers: List<ExceptionHandler>,
    private val networkList: NetworkList,
    private val routes: Map<String, Route>,
    private val socketTimeout: Int,
    private val readTimeout: Int,
    private val service: ExecutorService,
    host: String,
    port: Int
) : ServerSocketChannelEngine(InetSocketAddress(host, port), service) {

    override fun start() {
        synchronized(routes) {
            routes.forEach { (_, route) ->
                if (route is RunnableRoute) {
                    service.execute(route)
                }
            }
        }
        super.start()
    }

    override fun onAccept(channel: SocketChannel): Boolean =
            networkList.permits(channel.socket().inetAddress).also { isPermitted ->
                if (isPermitted) {
                    channel.socket().soTimeout = socketTimeout
                }
            }

    @Throws(
        IOException::class,
        TimeoutException::class,
        BadRequestException::class)
    override fun onRead(channel: SocketChannel) {
        val messageChannel: MessageChannel = factory.create(channel)
        try {
            val message: Message = messageChannel.read(readTimeout, TimeUnit.MILLISECONDS)
            if (message is http.message.Request) {
                val session: HttpSession = sessionFactory.create(channel, message)
                routes[message.path]?.onRoute(session).also {
                    messageChannel.write(session.response)
                    if (!session.keepAlive) {
                        session.close()
                    } else if (session.isUpgrade) {
                        unregister(channel)
                    }
                } ?: throw BadRequestException("Path (path = \"${message.path}\") does not exist.")
            } else {
                throw BadRequestException("Expecting a Request to be sent.")
            }
        } catch (ex: HttpException) {
            messageChannel.write(httpExceptionHandlers.getResponse(ex, ex::class))
            channel.close()
        }
    }

    override fun onException(ex: Exception) {
        exceptionHandlers.forEach {
            it.onException(ex)
        }
    }

    data class Builder(
        private val host: String,
        private val service: ExecutorService
    ) {
        private var factory: MessageChannelFactory = MessageBufferChannelFactory()
        private var sessionFactory: HttpSessionFactory<SocketChannel> = SocketChannelHttpSessionFactory()
        private val httpExceptionHandlers: MutableMap<KClass<*>, HttpExceptionInterceptor<*>> = mutableMapOf()
        private val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()
        private var networkList: NetworkList = EmptyNetworkList()
        private var routes: MutableMap<String, Route> = mutableMapOf()
        private var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
        private var readTimeout: Int = DEFAULT_READ_TIMEOUT
        private var port: Int = DEFAULT_HTTP_PORT

        fun setPort(port: Int) = apply {
            this.port = port
        }

        fun setChannelFactory(factory: MessageChannelFactory) = apply {
            this.factory = factory
        }

        fun setSessionFactory(factory: HttpSessionFactory<SocketChannel>) = apply {
            sessionFactory = factory
        }

        fun setNetworkList(list: NetworkList) = apply {
            networkList = list
        }

        fun addHttpExceptionHandler(handler: HttpExceptionInterceptor<*>) = apply {
            httpExceptionHandlers[handler.type] = handler
        }

        fun addExceptionHandler(handler: ExceptionHandler) = apply {
            exceptionHandlers.add(handler)
        }

        fun setRoutes(routes: Set<Route>) = apply {
            routes.forEach { route ->
                this.routes[route.path] = route
            }
        }

        fun addRoute(route: Route) = apply {
            routes[route.path] = route
        }

        fun setSocketTimeout(timeout: Int) = apply {
            socketTimeout = timeout
        }

        fun setReadTimeout(timeout: Int) = apply {
            readTimeout = timeout
        }

        fun build() = HttpEngine(
            factory = factory,
            sessionFactory = sessionFactory,
            httpExceptionHandlers = httpExceptionHandlers,
            exceptionHandlers = exceptionHandlers,
            networkList = networkList,
            routes = routes,
            socketTimeout = socketTimeout,
            readTimeout = readTimeout,
            service = service,
            host = host,
            port = port
        )
    }

    companion object {
        private const val DEFAULT_HTTP_PORT = 80
        private const val DEFAULT_SOCKET_TIMEOUT = 60000
        private const val DEFAULT_READ_TIMEOUT = 30000

        @Suppress("UNCHECKED_CAST")
        private fun <T : HttpException> Map<KClass<*>, HttpExceptionInterceptor<*>>.getResponse(exception: HttpException, type: KClass<T>): Response {
            val handler: HttpExceptionInterceptor<T>? =  get(type)?.let { it as HttpExceptionInterceptor<T> }
            return handler?.onException(exception as T) ?: exception.response
        }
    }
}