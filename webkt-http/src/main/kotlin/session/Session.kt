package http.session

import http.Transaction
import java.io.Closeable

/**
 * A object to represent the HTTP state-less session of the connection.
 */
interface Session<T> : Closeable {
    /** Unique identifier */
    val id: String
    /** Time the Session was created. */
    val created: Long
    /** The channel, used for communicate between the connections. */
    val channel: T
    /** Set of transactions between the connections. */
    val history: Set<Transaction>
    /** Keep the session alive? */
    val keepAlive: Boolean
}