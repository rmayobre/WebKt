package http

import http.path.RunnablePath
import java.util.concurrent.ExecutorService

class HttpExecutorService(
    private val paths: Set<RunnablePath>,
    private val service: ExecutorService
) : ExecutorService by service {

    init {
        paths.forEach { path ->
            service.execute(path)
        }
    }

    override fun shutdown() {
        paths.forEach { path ->
            path.stop()
        }
        service.shutdown()
    }
}