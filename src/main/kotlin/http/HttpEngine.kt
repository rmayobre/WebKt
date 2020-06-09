package http

import ServerSocketChannelEngine
import http.exception.BadRequestException
import http.exception.HttpException
import http.exception.HttpExceptionHandler
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
    private val exceptionHandlers: Map<KClass<*>, HttpExceptionHandler<*>>,
    private val networkList: NetworkList,
    private val routes: Map<String, Route>,
    private val socketTimeout: Int,
    private val readTimeout: Int,
    private val service: ExecutorService,
    host: String,
    port: Int
) : ServerSocketChannelEngine(InetSocketAddress(host, port), service) {

    constructor(host: String, service: ExecutorService):
        this(host, DEFAULT_HTTP_PORT, service)

    constructor(host: String, port: Int, service: ExecutorService) : this(
        factory = MessageBufferChannelFactory(),
        sessionFactory = SocketChannelHttpSessionFactory(),
        exceptionHandlers = mutableMapOf(),
        networkList = EmptyNetworkList(),
        routes = mutableMapOf(),
        socketTimeout = DEFAULT_SOCKET_TIMEOUT,
        readTimeout = DEFAULT_READ_TIMEOUT,
        service = service,
        host = host,
        port = port
    )

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
                routes[message.path]?.onRoute(session)?.also { response ->
                    messageChannel.write(response)
                    if (session.keepAlive) {
                        register(channel)
                    } else {
                        session.close()
                    }
                } ?: throw BadRequestException("Path does not exist.")
            } else {
                throw BadRequestException("Expecting a Request to be sent.")
            }
        } catch (ex: HttpException) {
            println("HttpException (Message): ${ex.message}")
            println("HttpException (Reason): ${ex.reason}")
            println("HttpException (Response): ${ex.response}")
            println("HttpException (Body): ${ex.body}")
            messageChannel.write(exceptionHandlers.getResponse(ex, ex::class))
            channel.close()
        }
    }

    override fun onException(ex: Exception) {
        println("Exception: ${ex.message}")
        println("Exception: ${ex.stackTrace}")
    }

    data class Builder(
        private val host: String,
        private val service: ExecutorService
    ) {
        private var factory: MessageChannelFactory = MessageBufferChannelFactory()
        private var sessionFactory: HttpSessionFactory<SocketChannel> = SocketChannelHttpSessionFactory()
        private val exceptionHandlers: MutableMap<KClass<*>, HttpExceptionHandler<*>> = mutableMapOf()
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

        fun addExceptionHandler(handler: HttpExceptionHandler<*>) = apply {
            exceptionHandlers[handler.type] = handler
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
        private fun <T : HttpException> Map<KClass<*>, HttpExceptionHandler<*>>.getResponse(exception: HttpException, type: KClass<T>): Response {
            val handler: HttpExceptionHandler<T>? =  get(type)?.let { it as HttpExceptionHandler<T> }
            return handler?.handle(exception as T) ?: exception.response
        }
    }
}