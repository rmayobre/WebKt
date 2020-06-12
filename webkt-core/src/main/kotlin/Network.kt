import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException


object Network {
    private const val DEFAULT_PING_TIMEOUT = 1000

    val localHost: InetAddress?
        get() = try {
            InetAddress.getLocalHost()
        } catch (ex: UnknownHostException) {
            null
        }

    /**
     * A blocking scan on local network for reachable addresses. Returns an empty list if there are no other connections available
     * in the network, or applications could not connect to network.
     */
    fun scanLocalNetworks(timeout: Int = DEFAULT_PING_TIMEOUT): List<InetAddress> {
        val addresses: MutableList<InetAddress> = mutableListOf()
        scanLocalNetworks(timeout) { address ->
            addresses.add(address)
        }
        return addresses
    }

    fun scanLocalNetworks(timeout: Int = DEFAULT_PING_TIMEOUT, block: (address: InetAddress) -> Unit) = localHost?.let { localAddress ->
        val localIp: ByteArray = localAddress.address
        for (i in 0..255) {
            if (i.toByte() != localIp[3]) {
                val ip: ByteArray = byteArrayOf(localIp[0], localIp[1], localIp[2], i.toByte())
                val address: InetAddress = InetAddress.getByAddress(ip)
                if (address.isReachable(timeout)) {
                    block(address)
                }
            }
        }
    }

    fun scanLocalPorts(port: Int, block: (address: InetSocketAddress) -> Unit) = scanLocalPorts(port, DEFAULT_PING_TIMEOUT, block)

    fun scanLocalPorts(port: Int, timeout: Int, block: (address: InetSocketAddress) -> Unit) = localHost?.let { localAddress ->
        val localIp: ByteArray = localAddress.address
        val socket = Socket()
        for (i in 0..255) {
            if (i.toByte() != localIp[3]) {
                val ip: ByteArray = byteArrayOf(localIp[0], localIp[1], localIp[2], i.toByte())
                val address = InetSocketAddress(InetAddress.getByAddress(ip), port)
                try {
                    socket.connect(address, timeout)
                    block(address)
                    socket.close()
                } catch (ex: Exception) {
                    try {
                        socket.close()
                    } catch (ex: IOException) {}
                }
            }
        }
    }

    fun ping(address: InetAddress, timeout: Int = DEFAULT_PING_TIMEOUT): Boolean = address.isReachable(timeout)
}