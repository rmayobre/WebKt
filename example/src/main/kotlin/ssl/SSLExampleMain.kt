package example.ssl

import SSLServerSocketChannelEngine
import SSLSocketChannel
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

import javax.net.ssl.SSLSocketFactory

private val threadPool = ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, LinkedBlockingDeque(), ThreadFactory { runnable ->
    Thread(runnable).apply {
        setUncaughtExceptionHandler { thread, throwable ->
            println("${thread.name} (Uncaught Error): ${throwable.message}")
        }
    }
})

private val context: SSLContext = SSLContext.getDefault()

private object ExampleSSLServerEngine: SSLServerSocketChannelEngine(
    context = context,
    address = InetSocketAddress("localhost", 8080),
    service = threadPool
) {
    override fun onAccept(channel: SocketChannel): Boolean {
        println("New connection -> ${channel.remoteAddress}")
        return true
    }

    override fun onRead(channel: SSLSocketChannel) {
        val buffer = ByteBuffer.allocate(48)
        val numOfBytesRead = channel.read(buffer)
        println("Number of byte read: $numOfBytesRead")
        println("Data read: ${buffer.int}")
    }

    override fun onException(ex: Exception) {
        println(ex)
    }

}

fun main() {
    ExampleSSLServerEngine.start()
    try {
        val sslsocketfactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val sslsocket = sslsocketfactory.createSocket("localhost", 8080) as SSLSocket
        val input: InputStream = sslsocket.inputStream
        val output: OutputStream = sslsocket.outputStream

        // Write a test byte to get a reaction :)
        output.write(1)
        while (input.available() > 0) {
            println(input.read())
        }
        println("Successfully connected")
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}
