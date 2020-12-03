package http.route

import java.io.Closeable

/**
 * A route that requires a threaded background process. Submitting
 * this type of path to the HttpEngine will result in the path being
 * submitted to the ExecutorService once the path has been added to the
 * Builder.
 */
interface RunnableRoute : Route, Runnable, Closeable {
    val isRunning: Boolean
}