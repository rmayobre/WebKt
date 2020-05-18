import java.util.concurrent.TimeUnit

class SSLServerSocketChannelEngine : ServerEngine {

    private var thread: Thread? = null

    override fun start(blocking: Boolean) {
        val runnable = if (blocking) {
            // TODO finish blocking runnable
            BlockingRunnable()
        } else {
            // TODO finish nonblocking runnable.
            NonBlockingRunnable()
        }
        thread = Thread(runnable, THREAD_NAME).also { it.start() }
    }

    override fun stop(timeout: Long, timeUnit: TimeUnit) {
        TODO("Not yet implemented")
    }

    private inner class BlockingRunnable : Runnable {
        override fun run() {
            TODO("Not yet implemented")
        }
    }

    private inner class NonBlockingRunnable : Runnable {
        override fun run() {
            TODO("Not yet implemented")
        }
    }

    companion object {
        private const val THREAD_NAME = "ssl-server-engine-webkt"
    }
}