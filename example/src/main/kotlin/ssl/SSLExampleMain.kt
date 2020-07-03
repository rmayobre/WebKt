package example.ssl

import SSLServerSocketChannelEngine
import SSLSocketChannel
import java.io.*
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.file.Paths
import java.security.KeyStore
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


private var KEYSTORE = Paths.get("", "keystore.jks").toString()
private const val STORETYPE = "JKS"
private const val STOREPASSWORD = "storepassword"
private const val KEYPASSWORD = "keypassword"

private val threadPool = ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, LinkedBlockingDeque(), ThreadFactory { runnable ->
    Thread(runnable).apply {
        setUncaughtExceptionHandler { thread, throwable ->
            println("${thread.name} (Uncaught Error): ${throwable.message}")
        }
    }
})

private class ExampleSSLServerEngine(context: SSLContext): SSLServerSocketChannelEngine(
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
    try {
        val keyStoreFile = File(KEYSTORE)
        val keyStore = KeyStore.getInstance(STORETYPE)
        keyStore.load(FileInputStream(keyStoreFile), STOREPASSWORD.toCharArray())

        val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
        keyManagerFactory.init(keyStore, KEYPASSWORD.toCharArray())

        val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
        trustManagerFactory.init(keyStore)

        val context: SSLContext = SSLContext.getInstance( "TLS" )
        context.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null )

        /* Start server */
        val server = ExampleSSLServerEngine(context)
        server.start()

        val sslsocketfactory = context.socketFactory
        val sslsocket = sslsocketfactory.createSocket("localhost", 8080) as SSLSocket
        sslsocket.useClientMode = true
        sslsocket.startHandshake()
//        sslsocket.needClientAuth = true
        sslsocket.addHandshakeCompletedListener {
            println("HandshakeComplete!")
            println(it.session)
        }
        val input: InputStream = sslsocket.inputStream
        val output: OutputStream = sslsocket.outputStream

        // Write a test byte to get a reaction :)
        output.write("Hello".toByteArray())
        while (input.available() > 0) {
            println(input.read())
        }
        println("Successfully connected")
        printSocketInfo(sslsocket)
        output.write(123454254)
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}

//fun main() {
//    val keyStoreFile = File(KEYSTORE)
//    val keyStore = KeyStore.getInstance(STORETYPE)
//    keyStore.load(FileInputStream(keyStoreFile), STOREPASSWORD.toCharArray())
//
//    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
//    keyManagerFactory.init(keyStore, KEYPASSWORD.toCharArray())
//
//    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
//    trustManagerFactory.init(keyStore)
//
//    val context: SSLContext = SSLContext.getInstance( "TLS" )
//    context.init(
//        keyManagerFactory.keyManagers,
//        trustManagerFactory.trustManagers,
//        null )
//
//    /* Start server */
//    val server = ExampleSSLServerEngine(context)
//    server.start()
//
//
//    val `in` = BufferedReader(
//        InputStreamReader(System.`in`))
//    val out = System.out
//    val f = context.socketFactory//SSLSocketFactory.getDefault() as SSLSocketFactory
//    try {
//        val c = f.createSocket("localhost", 8888) as SSLSocket
//        printSocketInfo(c)
//        c.startHandshake()
//        val w = BufferedWriter(
//            OutputStreamWriter(c.outputStream))
//        val r = BufferedReader(
//            InputStreamReader(c.inputStream))
//        var m: String?
//        while (r.readLine().also { m = it } != null) {
//            out.println(m)
//            m = `in`.readLine()
//            w.write(m!!, 0, m!!.length)
//            w.newLine()
//            w.flush()
//        }
//        w.close()
//        r.close()
//        c.close()
//    } catch (e: IOException) {
//        System.err.println(e.toString())
//    }
//}

private fun printSocketInfo(s: SSLSocket) {
    println("Socket class: " + s.javaClass)
    println("   Remote address = "
        + s.inetAddress.toString())
    println("   Remote port = " + s.port)
    println("   Local socket address = "
        + s.localSocketAddress.toString())
    println("   Local address = "
        + s.localAddress.toString())
    println("   Local port = " + s.localPort)
    println("   Need client authentication = "
        + s.needClientAuth)
    val ss = s.session
    println("   Cipher suite = " + ss.cipherSuite)
    println("   Protocol = " + ss.protocol)
    println("   HandshakeSession = ${s.handshakeSession}")
    println("   SSLSession = ${s.session}")
}