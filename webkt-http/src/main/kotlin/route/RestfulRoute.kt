package http.route

import http.Method
import http.exception.BadRequestException
import http.exception.HttpException
import http.session.HttpSession
import java.io.File
import java.net.URL
import java.nio.file.Files

open class RestfulRoute(override val path: String) : Route {

    override fun onRoute(session: HttpSession) {
        when (session.request.method) {
            Method.GET -> onGet(session)
            Method.PUT -> onPut(session)
            Method.POST -> onPost(session)
            Method.DELETE -> onDelete(session)
            Method.HEAD -> onHead(session)
            Method.OPTIONS -> onOptions(session)
            Method.TRACE -> onTrace(session)
            Method.CONNECT -> onConnect(session)
        }
    }

    @Throws(HttpException::class)
    protected open fun onGet(session: HttpSession) {
        throw BadRequestException("This path does not support GET method.")
    }

    @Throws(HttpException::class)
    protected open fun onPut(session: HttpSession) {
        throw BadRequestException("This path does not support PUT method.")
    }

    @Throws(HttpException::class)
    protected open fun onPost(session: HttpSession) {
        throw BadRequestException("This path does not support POST method.")
    }

    @Throws(HttpException::class)
    protected open fun onDelete(session: HttpSession) {
        throw BadRequestException("This path does not support DELETE method.")
    }

    @Throws(HttpException::class)
    protected open fun onHead(session: HttpSession) {
        throw BadRequestException("This path does not support HEAD method.")
    }

    @Throws(HttpException::class)
    protected open fun onOptions(session: HttpSession) {
        throw BadRequestException("This path does not support OPTIONS method.")
    }

    @Throws(HttpException::class)
    protected open fun onTrace(session: HttpSession) {
        throw BadRequestException("This path does not support TRACE method.")
    }

    @Throws(HttpException::class)
    protected open fun onConnect(session: HttpSession) {
        throw BadRequestException("This path does not support CONNECT method.")
    }

    /**
     * Loads a Web page from a resource file inside the resource directory. If
     * resource could not be loaded, then sends back a 404 page.
     * @param resource file from resource directory.
     */
    protected fun loadWebPageAsString(resource: String): String =
        loadWebPageAsStringOrNull(resource) ?: WEB_PAGE_NOT_FOUND

    /**
     * Loads a Web page from a resource file inside the resource directory. If
     * resource could not be loaded, returns null.
     * @param resource file from resource directory.
     */
    protected fun loadWebPageAsStringOrNull(resource: String): String? =
        (javaClass.classLoader.getResource(resource))?.let { resourceURL: URL ->
            String(Files.readAllBytes(File(resourceURL.file).toPath()))
        }

    companion object {
        private const val WEB_PAGE_NOT_FOUND: String = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<title>404 Not Found</title>" +
            "</head>" +
            "<body>" +
            "" +
            "<h1>404 - Not Found.</h1>" +
            "" +
            "</body>" +
            "</html>"
    }
}