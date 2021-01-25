import channel.tcp.SecureServerSocketChannel

fun test() {
    val server = SecureServerSocketChannel.open()

    val client = server.accept()
}