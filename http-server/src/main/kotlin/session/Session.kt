package session

import TypeChannel
import javax.net.ssl.SSLSession

/**
 * A object to represent the state-less session of the connection.
 * @param T dictates the type of data expected to be pass through this session.
 */
interface Session<T> {
    /** Unique identifier */
    val id: String

    /** Time the Session was created. */
    val created: Long

    /** A Session's SSL Session context. Is null if Session has no SSL. */
    val sslSession: SSLSession?

    /** The channel, used for communicate between the connections. */
    val channel: TypeChannel<T>

    /** Get a copy of all transactions between the connections. */
    val history: Set<T>

    /**
     * A pending response or request (defined by the ServerEngine's event) that is
     * waiting to be handled. A pending request of response can be null, in this
     * event it is possible the channel has not be read yet.
     */
    var pending: T?
}