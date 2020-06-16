package http.route

import http.Method
import http.Status
import http.content.ContentType
import http.content.TextType
import http.exception.BadRequestException
import http.exception.HttpException
import http.exception.NotFoundException
import http.message.Response
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

    protected fun HttpSession.loadResourceAsResponse(resource: String, contentType: ContentType) {
        response = loadResourceAsStringOrNull(resource)?.let { resourceStr ->
            Response.Builder(Status.OK)
                .addHeader("Content-Type", contentType.headerValue)
                .addHeader("Connection", "close")
                .setBody(resourceStr)
                .build()
        } ?: when (contentType) {
            is TextType -> if (contentType.subType == "html") {
                Response.Builder(Status.NOT_FOUND)
                    .addHeader("Content-Type", contentType.headerValue)
                    .addHeader("Connection", "close")
                    .setBody(loadResourceAsString(DEFAULT_NOT_FOUND_HTML))
                    .build()
            } else {
                throw NotFoundException()
            }
            else -> throw NotFoundException()
        }

    }

    /**
     * Loads a Web page from a resource file inside the resource directory. If
     * resource could not be loaded, then sends back a 404 page.
     * @param resource file from resource directory.
     */
    protected fun loadResourceAsString(resource: String): String =
        loadResourceAsStringOrNull(resource) ?: throw NotFoundException()

    /**
     * Loads a Web page from a resource file inside the resource directory. If
     * resource could not be loaded, returns null.
     * @param resource file from resource directory.
     */
    protected fun loadResourceAsStringOrNull(resource: String): String? =
        (javaClass.classLoader.getResource(resource))?.let { resourceURL: URL ->
            String(Files.readAllBytes(File(resourceURL.file).toPath()))
        }

    companion object {
        private const val DEFAULT_NOT_FOUND_HTML = "DEFAULT_404_NOT_FOUND.html"
    }
}