import engine.ServerSocketChannelEngine
import exception.BadRequestException
import exception.ExceptionHandler
import exception.HttpException
import exception.HttpExceptionInterceptor
import message.Message
import message.Response
import message.channel.MessageChannel
import router.Router
import session.Session
import session.factory.SessionFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

/**
 * HTTP protocol layer of the channel.ServerSocketChannelEngine inheritance.
 */
abstract class HttpEngine protected constructor(
    address: InetSocketAddress,
    service: ExecutorService,
    var readTimeout: Int = DEFAULT_READ_TIMEOUT,
    var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
) : ServerSocketChannelEngine(address, service) {

    //    private val routes: MutableMap<String, Route> = mutableMapOf(),
    protected abstract val router: Router //TODO finish the router class.

//    protected abstract val channelFactory: MessageChannelFactory

    protected abstract val sessionFactory: SessionFactory<Message>

    protected open val httpExceptionHandlers: MutableMap<KClass<*>, HttpExceptionInterceptor<*>> = mutableMapOf()

    protected open val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()

    protected open val networkList: NetworkList = EmptyNetworkList()

    /*
    TODO the new lifescycle

    1. OnChannelAccepted - Create an HTTPSession, attach to channel
    2. onRead - Read from HTTPSession.
    3. onWrite - write from HTTPSession.
     */

    @Throws(IOException::class)
    override fun onChannelAccepted(channel: SocketChannel) {
        if (networkList.permits(channel.socket().inetAddress)) {
            channel.socket().soTimeout = socketTimeout
            registerToRead(
                channel = channel,
                attachment = sessionFactory.create(channel)
            )
        } else {
            channel.close()
        }
    }

    /*
    THINK ABOUT THE LIFECYCLE

    This will be implemented as a class built by the use to be implemented in their application.
     */
    @Throws(
        IOException::class,
        TimeoutException::class,
        BadRequestException::class)
    override fun onReadChannel(channel: SocketChannel, attachment: Any?) {
        val session = attachment as Session<Message>
//        val messageChannel: MessageChannel = attachment as MessageChannel
//        // TODO get session from attachment.
//        try {
//            val message: Message = messageChannel.read(readTimeout, TimeUnit.MILLISECONDS)
//            if (message is Request) {
//                val session: Session = sessionFactory.create(channel, message)
////                onNewSession(session)
//                TODO("Implement router logic.")
////                routes[message.path]?.onRoute(session).also {
////                    messageChannel.write(session.response)
////                    if (!session.keepAlive) {
////                        session.close()
////                    } else if (session.isUpgrade) {
//////                        unregister(channel)
////                    }
////                } ?: throw BadRequestException("Path (path = \"${message.path}\") does not exist.")
//            } else {
//                throw BadRequestException("Expecting a Request to be sent.")
//            }
//        } catch (ex: HttpException) {
//            messageChannel.write(httpExceptionHandlers.getResponse(ex, ex.javaClass.kotlin))
//            channel.close()
//        }
    }

    @Throws(IOException::class)
    override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
        val messageChannel = attachment as MessageChannel

        TODO("Not yet implemented")
    }

    override fun onException(channel: SelectableChannel, attachment: Any?, ex: Exception) {
        when (ex) {
            is HttpException -> {
                val messageChannel = attachment as MessageChannel
                messageChannel.write(httpExceptionHandlers.getResponse(ex, ex.javaClass.kotlin))
                channel.close()
            }
            else -> {
                ex.printStackTrace()
                TODO("Create a default response for exceptions.")
            }
        }
    }

//    /**
//     *
//     * @param address to be added to the NetworkList
//     * @see NetworkList
//     */
//    fun addAddressToList(address: InetAddress): Boolean =
//        when(networkList) {
//            is AllowList -> networkList.add(address)
//            is BlockList -> networkList.add(address)
//            is EmptyNetworkList -> false
//        }


//    /**
//     * The engine created
//     */
//    @Throws(IOException::class)
//    protected abstract fun onNewSession(session: HttpSession)



//    override fun start() {
//        synchronized(routes) {
//            routes.forEach { (_, route) ->
//                if (route is RunnableRoute) {
//                    service.execute(route)
//                }
//            }
//        }
//        super.start()
//    }
//
//    @Throws(IOException::class)
//    override fun onChannelAccepted(channel: SocketChannel) {
//        if (networkList.permits(channel.socket().inetAddress)) {
//            channel.socket().soTimeout = socketTimeout
//        } else {
//            channel.close()
//        }
//    }
////    override fun onAccept(channel: SocketChannel): Boolean =
////            networkList.permits(channel.socket().inetAddress).also { isPermitted ->
////                if (isPermitted) {
////                    channel.socket().soTimeout = socketTimeout
////                }
////            }
//
//    @Throws(
//        IOException::class,
//        TimeoutException::class,
//        BadRequestException::class)
//    override fun onReadChannel(channel: SocketChannel, attachment: Any?) { //TODO figure out the registry
//        val messageChannel: MessageChannel = factory.create(channel)
//        try {
//            val message: Message = messageChannel.read(readTimeout, TimeUnit.MILLISECONDS)
//            if (message is Request) {
//                val session: HttpSession = sessionFactory.create(channel, message)
//                routes[message.path]?.onRoute(session).also {
//                    messageChannel.write(session.response)
//                    if (!session.keepAlive) {
//                        session.close()
//                    } else if (session.isUpgrade) {
////                        unregister(channel)
//                    }
//                } ?: throw BadRequestException("Path (path = \"${message.path}\") does not exist.")
//            } else {
//                throw BadRequestException("Expecting a Request to be sent.")
//            }
//        } catch (ex: HttpException) {
////            messageChannel.write(httpExceptionHandlers.getResponse(ex, ex::class))
//            channel.close()
//        }
//    }
//
//    override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onException(ex: Exception, key: SelectionKey) {
//        exceptionHandlers.forEach {
//            it.onException(ex)
//        }
//    }
//
//    override fun onException(ex: Exception, channel: SocketChannel) {
//        TODO("Not yet implemented")
//    }

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

///**
// * HTTP protocol layer of the channel.ServerSocketChannelEngine inheritance.
// */
//open class HttpEngine protected constructor(
//    private val factory: MessageChannelFactory,
//    private val sessionFactory: HttpSessionFactory<SocketChannel>,
//    private val httpExceptionHandlers: Map<KClass<*>, HttpExceptionInterceptor<*>>,
//    private val exceptionHandlers: List<ExceptionHandler>,
//    private val networkList: NetworkList,
//    private val routes: Map<String, Route>,
//    private val socketTimeout: Int,
//    private val readTimeout: Int,
//    private val service: ExecutorService,
//    host: String,
//    port: Int
//) : ServerSocketChannelEngine(InetSocketAddress(host, port), service) {
//
//    override fun start() {
//        synchronized(routes) {
//            routes.forEach { (_, route) ->
//                if (route is RunnableRoute) {
//                    service.execute(route)
//                }
//            }
//        }
//        super.start()
//    }
//
//    @Throws(IOException::class)
//    override fun onChannelAccepted(channel: SocketChannel) {
//        if (networkList.permits(channel.socket().inetAddress)) {
//            channel.socket().soTimeout = socketTimeout
//        } else {
//            channel.close()
//        }
//    }
////    override fun onAccept(channel: SocketChannel): Boolean =
////            networkList.permits(channel.socket().inetAddress).also { isPermitted ->
////                if (isPermitted) {
////                    channel.socket().soTimeout = socketTimeout
////                }
////            }
//
//    @Throws(
//        IOException::class,
//        TimeoutException::class,
//        BadRequestException::class)
//    override fun onReadChannel(channel: SocketChannel, attachment: Any?) { //TODO figure out the registry
//        val messageChannel: MessageChannel = factory.create(channel)
//        try {
//            val message: Message = messageChannel.read(readTimeout, TimeUnit.MILLISECONDS)
//            if (message is Request) {
//                val session: HttpSession = sessionFactory.create(channel, message)
//                routes[message.path]?.onRoute(session).also {
//                    messageChannel.write(session.response)
//                    if (!session.keepAlive) {
//                        session.close()
//                    } else if (session.isUpgrade) {
////                        unregister(channel)
//                    }
//                } ?: throw BadRequestException("Path (path = \"${message.path}\") does not exist.")
//            } else {
//                throw BadRequestException("Expecting a Request to be sent.")
//            }
//        } catch (ex: HttpException) {
////            messageChannel.write(httpExceptionHandlers.getResponse(ex, ex::class))
//            channel.close()
//        }
//    }
//
//    override fun onWriteChannel(channel: SocketChannel, attachment: Any?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onException(ex: Exception, key: SelectionKey) {
//        exceptionHandlers.forEach {
//            it.onException(ex)
//        }
//    }
//
//    override fun onException(ex: Exception, channel: SocketChannel) {
//        TODO("Not yet implemented")
//    }
//
//    data class Builder(
//        private val host: String,
//        private val service: ExecutorService
//    ) {
//        private var factory: MessageChannelFactory = MessageBufferChannelFactory()
//        private var sessionFactory: HttpSessionFactory<SocketChannel> = SocketChannelHttpSessionFactory()
//        private val httpExceptionHandlers: MutableMap<KClass<*>, HttpExceptionInterceptor<*>> = mutableMapOf()
//        private val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()
//        private var networkList: NetworkList = EmptyNetworkList()
//        private var routes: MutableMap<String, Route> = mutableMapOf()
//        private var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT
//        private var readTimeout: Int = DEFAULT_READ_TIMEOUT
//        private var port: Int = DEFAULT_HTTP_PORT
//
//        fun setPort(port: Int) = apply {
//            this.port = port
//        }
//
//        fun setChannelFactory(factory: MessageChannelFactory) = apply {
//            this.factory = factory
//        }
//
//        fun setSessionFactory(factory: HttpSessionFactory<SocketChannel>) = apply {
//            sessionFactory = factory
//        }
//
//        fun setNetworkList(list: NetworkList) = apply {
//            networkList = list
//        }
//
//        fun addHttpExceptionHandler(handler: HttpExceptionInterceptor<*>) = apply {
//            httpExceptionHandlers[handler.type] = handler
//        }
//
//        fun addExceptionHandler(handler: ExceptionHandler) = apply {
//            exceptionHandlers.add(handler)
//        }
//
//        fun setRoutes(routes: Set<Route>) = apply {
//            routes.forEach { route ->
//                this.routes[route.path] = route
//            }
//        }
//
//        fun addRoute(route: Route) = apply {
//            routes[route.path] = route
//        }
//
//        fun setSocketTimeout(timeout: Int) = apply {
//            socketTimeout = timeout
//        }
//
//        fun setReadTimeout(timeout: Int) = apply {
//            readTimeout = timeout
//        }
//
//        fun build() = HttpEngine(
//            factory = factory,
//            sessionFactory = sessionFactory,
//            httpExceptionHandlers = httpExceptionHandlers,
//            exceptionHandlers = exceptionHandlers,
//            networkList = networkList,
//            routes = routes,
//            socketTimeout = socketTimeout,
//            readTimeout = readTimeout,
//            service = service,
//            host = host,
//            port = port
//        )
//    }
//
//    companion object {
//        private const val DEFAULT_HTTP_PORT = 80
//        private const val DEFAULT_SOCKET_TIMEOUT = 60000
//        private const val DEFAULT_READ_TIMEOUT = 30000
//
//        @Suppress("UNCHECKED_CAST")
//        private fun <T : HttpException> Map<KClass<*>, HttpExceptionInterceptor<*>>.getResponse(exception: HttpException, type: KClass<T>): Response {
//            val handler: HttpExceptionInterceptor<T>? =  get(type)?.let { it as HttpExceptionInterceptor<T> }
//            return handler?.onException(exception as T) ?: exception.response
//        }
//    }
//}