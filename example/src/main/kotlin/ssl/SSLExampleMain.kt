package example.ssl

import SSLServerSocketChannelEngine
import SSLSocketChannel
import sun.security.ssl.SSLServerSocketFactoryImpl
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
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

    override fun onAcceptSSLChannel(channel: SSLSocketChannel) {
        if (!channel.performHandshake()) {
            println("Connection failed -> ${channel.remoteAddress}")
            channel.close()
        } else {
            println("New connection -> ${channel.remoteAddress}")
        }
    }

    override fun onReadSSLChannel(channel: SSLSocketChannel) {
        val buffer = ByteBuffer.allocate(256)
        var numOfBytesRead = channel.read(buffer)
        numOfBytesRead = channel.read(buffer)
        println("Number of byte read: $numOfBytesRead")
        println("Data read: $buffer")
    }

    override fun onException(ex: Exception, key: SelectionKey) {
        println(ex)
    }

}

private class SocketServerTest(context: SSLContext): Runnable {
    private val serverSocket: SSLServerSocket = context.serverSocketFactory.createServerSocket(8080) as SSLServerSocket

    override fun run() {
        while(true) {
            serverSocket.accept()?.let { socket ->
                val input = socket.getInputStream()
                val bufferedReader = BufferedReader(InputStreamReader(input))
                var line: String? = bufferedReader.readLine()
                while (line != null) {
                    if (line == "hello") {
                        println("Goodbye")
                        socket.close()
                    } else {
                        println(line)

                        line = bufferedReader.readLine()
                    }
                }
            }
        }
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

        val context: SSLContext = SSLContext.getInstance( "TLSv1.1" )
        context.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null )

        /* Start server */
        val server = ExampleSSLServerEngine(context)
        server.start()

//        val server = SocketServerTest(context)
//        val thread = Thread(server, "thread-server")
//        thread.start()


        val sslsocketfactory = context.socketFactory
        val sslsocket = (sslsocketfactory.createSocket("localhost", 8080) as SSLSocket).apply {
            useClientMode = true
            enableSessionCreation = true
            startHandshake()
            addHandshakeCompletedListener {
                println("HandshakeComplete!")
                println(it.session)
            }
        }

        val input: InputStream = sslsocket.inputStream
        val output: OutputStream = sslsocket.outputStream

        // Write a test byte to get a reaction :)
//        output.write("Hello my name is ryan".toByteArray())
//        while (input.available() > 0) {
//            println(input.read())
//        }
        println("Successfully connected")
        printSocketInfo(sslsocket)
//        output.write(123454254)

        var theCharacter = 0
        theCharacter = System.`in`.read()
        while (theCharacter != '~'.toInt()) // The '~' is an escape character to exit
        {
            output.write(theCharacter)
            output.flush()
            theCharacter = System.`in`.read()
        }
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