package ssl

import old.ssl.SSLServerSocketChannelEngine
import old.ssl.SSLSocketChannel
import java.io.*
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectableChannel
import java.nio.file.Paths
import java.security.KeyStore
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

private var KEYSTORE = Paths.get("", "keystore.jks").toString()
private const val STORETYPE = "JKS"
private const val STOREPASSWORD = "storepassword"
private const val KEYPASSWORD = "keypassword"

private val threadPool = ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, LinkedBlockingDeque()) { runnable ->
    Thread(runnable).apply {
        setUncaughtExceptionHandler { thread, throwable ->
            println("${thread.name} (Uncaught Error): ${throwable.message}")
            throwable.printStackTrace()
        }
    }
}

private class ExampleSSLServerEngine(context: SSLContext): SSLServerSocketChannelEngine(
    context = context,
    address = InetSocketAddress("localhost", 8080),
    service = threadPool
) {

    override fun onSSLHandshake(channel: SSLSocketChannel) {
        if (!channel.performHandshake()) {
            println("Connection failed -> $channel")
            channel.close()
        } else {
            println("New connection -> $channel")
            println()
            if (channel.isOpen) {
                registerToRead(channel)
            }
        }
    }

    override fun onReadSSLChannel(channel: SSLSocketChannel) {
        try {
            val buffer = ByteBuffer.allocate(2560)
            val bytesRead = channel.read(buffer)
            if (bytesRead > 0) {
                val data = String(buffer.array())
                println("Client Data Received: ")
                println("Number of byte read: $bytesRead")
                println("Data read: $data")
                println()
                registerToWrite(channel, data)
            } else {
                registerToRead(channel)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    override fun onWriteSSLChannel(channel: SSLSocketChannel, data: Any?) {
        channel.write(ByteBuffer.wrap(("${data as String}/r/n").toByteArray()))
        registerToRead(channel)
    }

    override fun onException(channel: SelectableChannel, attachment: Any?, ex: Exception) {
        println("Exception on channel: $channel -> $ex")
        ex.printStackTrace()
    }

    override fun onException(channel: SSLSocketChannel, attachment: Any?, ex: Exception) {
        println("Exception on SSLSocketChannel: $channel -> $ex")
        ex.printStackTrace()
    }

}

/**
 * Main function for the SSL example. Generate a Keystore JKS file with
 * the following line (place at the root of project in order to run ssl.main):
 *
 * keytool -genkey -keyalg RSA -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
 */
fun main() {
    try {
        val keyStoreFile = File(KEYSTORE)
        val keyStore = KeyStore.getInstance(STORETYPE)
        keyStore.load(FileInputStream(keyStoreFile), STOREPASSWORD.toCharArray())

        val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
        keyManagerFactory.init(keyStore, KEYPASSWORD.toCharArray())

        val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
        trustManagerFactory.init(keyStore)

        val context: SSLContext = SSLContext.getInstance("TLSv1.2")
        context.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null)

        /* Start server */
        val server = ExampleSSLServerEngine(context)
        server.start()

        val channel = SSLSocketChannel.client(
            address = InetSocketAddress("localhost", 8080),
            context = context
        ).apply {
            performHandshake()
        }

        val buffer = ByteBuffer.wrap("hello".toByteArray())

        while(buffer.hasRemaining()) {
            channel.write(buffer)
        }

        println("SSLSocketChannel information: \n$channel")
        println()

        buffer.clear()
        buffer.flip()

        while (true) {
            var bytesRead: Int
            if (channel.read(buffer).also { bytesRead = it } > 0) {
                val str = String(buffer.array())
                if (str == "hello") {
                    println("Server Data Received: ")
                    println("Number of byte read: $bytesRead")
                    println("Buffer data: $str")
                    channel.close()
                    server.stop()
                    return
                }
            }
            buffer.clear()
        }
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}