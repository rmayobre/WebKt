package http.path

/**
 * An HTTP path that requires a threaded background process. Submitting
 * this type of path to the HttpEngine will result in the path being
 * submitted to the ExecutorService once the path has been added to the
 * Builder.
 */
interface RunnablePath : Path, Runnable {
    fun stop()
}