package http

import ServerSocketChannelEngine
import http.exception.BadRequestException
import http.exception.HttpException
import http.exception.HttpExceptionHandler
import http.message.Message
import http.message.Request
import http.message.Response
import http.message.reader.factory.MessageBufferReaderFactory
import http.message.writer.factory.MessageBufferWriterFactory
import http.message.reader.factory.MessageReaderFactory
import http.message.writer.factory.MessageWriterFactory
import http.message.reader.MessageReader
import http.message.writer.MessageWriter
import http.path.Path
import http.path.RunnablePath
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass

open class HttpEngine protected constructor(
    private val readerFactory: MessageReaderFactory,
    private val writerFactory: MessageWriterFactory,
    private val exceptionHandlers: Map<KClass<*>, HttpExceptionHandler<*>>,
    private val networkList: NetworkList,
    private val paths: Map<String, Path>,
    private val socketTimeout: Int,
    private val readTimeout: Int,
    private val blocking: Boolean,
    service: HttpExecutorService,
    address: InetAddress
) : ServerSocketChannelEngine(service, InetSocketAddress(address, DEFAULT_HTTP_PORT)) {

    override fun onAccept(channel: SocketChannel): Boolean {
        if (networkList.permits(channel.socket().inetAddress)) {
            channel.apply {
                configureBlocking(blocking)
                socket().apply {
                    soTimeout = socketTimeout
                }
            }
            return true
        }
        return false
    }

    @Throws(
        IOException::class,
        TimeoutException::class,
        BadRequestException::class)
    override fun onRead(key: SelectionKey) {
        val channel: SocketChannel = key.channel() as SocketChannel
        val writer: MessageWriter = writerFactory.create(channel)
        val reader: MessageReader = readerFactory.create(channel)
        try {
            val message: Message = reader.read(readTimeout, TimeUnit.MILLISECONDS)
            if (message is Request) {
                paths[message.path]?.onRequest(key, message)?.also { response ->
                    writer.write(response)
                } ?: throw BadRequestException("Path does not exist.")
            } else {
                throw BadRequestException("Expecting a Request to be sent.")
            }
        } catch (ex: HttpException) {
            writer.write(exceptionHandlers.getResponse(ex, ex::class))
        }
    }

    data class Builder(
        private val address: InetAddress,
        private val executor: ExecutorService
    ) {

        private var readerFactory: MessageReaderFactory = MessageBufferReaderFactory()

        private var writerFactory: MessageWriterFactory = MessageBufferWriterFactory()

        private val exceptionHandlers: MutableMap<KClass<*>, HttpExceptionHandler<*>> = mutableMapOf()

        private var networkList: NetworkList = EmptyNetworkList()

        private var runnablePaths: MutableSet<RunnablePath> = mutableSetOf()

        private var paths: MutableMap<String, Path> = mutableMapOf()

        private var socketTimeout: Int = DEFAULT_SOCKET_TIMEOUT

        private var readTimeout: Int = DEFAULT_READ_TIMEOUT

        private var blocking: Boolean = false

        fun configureBlocking(blocking: Boolean) = apply {
            this.blocking = blocking
        }

        fun setWriterFactory(factory: MessageWriterFactory) = apply {
            writerFactory = factory
        }

        fun setReaderFactory(factory: MessageReaderFactory) = apply {
            readerFactory = factory
        }

        fun setNetworkList(list: NetworkList) = apply {
            networkList = list
        }

        fun addExceptionHandler(handler: HttpExceptionHandler<*>) = apply {
            exceptionHandlers[handler.type] = handler
        }

        fun setPaths(paths: Set<Path>) = apply {
            paths.forEach { path ->
                if (path is RunnablePath) {
                    runnablePaths.add(path)
                } else {
                    this.paths[path.id] = path
                }
            }
        }

        fun addPath(path: RunnablePath) = apply {
            runnablePaths.add(path)
        }

        fun addPath(path: Path) = apply {
            paths[path.id] = path
        }

        fun setSocketTimeout(timeout: Int) = apply {
            socketTimeout = timeout
        }

        fun setReadTimeout(timeout: Int) = apply {
            readTimeout = timeout
        }

        fun build() = HttpEngine(
            readerFactory = readerFactory,
            writerFactory = writerFactory,
            exceptionHandlers = exceptionHandlers,
            networkList = networkList,
            paths = paths.apply {
                runnablePaths.forEach { path ->
                    put(path.id, path)
                }
            },
            socketTimeout = socketTimeout,
            readTimeout = readTimeout,
            blocking = blocking,
            service = HttpExecutorService(runnablePaths, executor),
            address = address)
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