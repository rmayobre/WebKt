package http.session

import tcp.TypeChannel
import javax.net.ssl.SSLSession

/**
 * A object to represent the HTTP state-less session of the connection.
 * @param T dictates the type of data expected to be pass through this session.
 */
interface Session<T> {
    /** Unique identifier */
    val id: String
    /** Time the Session was created. */
    val created: Long
    /** The channel, used for communicate between the connections. */
    val channel: TypeChannel<T>
    /** Set of transactions between the connections. */
    val history: Set<T>
    /** A Session's SSL Session context. Is null if Session has no SSL. */
    val sslSession: SSLSession?
//    /** Keep the session alive? */
//    val keepAlive: Boolean

    var pendingRequest: T?

    var pendingResponse: T?

//    val pendingRequest: T?
//
//    fun sendResponse(response: T)
}