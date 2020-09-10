package http

import java.net.InetAddress

/**
 * An immutable list of addresses used to determine if a network address
 * is allowed access to the server.
 */
sealed class NetworkList(addresses: Set<InetAddress>): Set<InetAddress> by addresses {
    abstract fun permits(address: InetAddress): Boolean
}

/**
 * Only these addresses will be allowed access to the server.
 */
class AllowList private constructor(
    private val addresses: MutableSet<InetAddress>
) : NetworkList(addresses), MutableCollection<InetAddress> by addresses {

    override val size: Int
        get() = addresses.size

    constructor(): this(mutableSetOf())

    override fun contains(element: InetAddress): Boolean {
        return addresses.contains(element)
    }

    override fun containsAll(elements: Collection<InetAddress>): Boolean {
        return addresses.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return addresses.isEmpty()
    }

    override fun iterator(): MutableIterator<InetAddress> {
        return addresses.iterator()
    }

    override fun permits(address: InetAddress): Boolean {
        return addresses.contains(address)
    }
}

/**
 * These addresses are not allowed access to server.
 */
class BlockList private constructor(
    private val addresses: MutableSet<InetAddress>
) : NetworkList(addresses), MutableCollection<InetAddress> by addresses {

    override val size: Int
        get() = addresses.size

    constructor(): this(mutableSetOf())

    override fun contains(element: InetAddress): Boolean {
        return addresses.contains(element)
    }

    override fun containsAll(elements: Collection<InetAddress>): Boolean {
        return addresses.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return addresses.isEmpty()
    }

    override fun iterator(): MutableIterator<InetAddress> {
        return addresses.iterator()
    }

    override fun permits(address: InetAddress): Boolean {
        return !addresses.contains(address)
    }
}

/** Accepts every network address. */
class EmptyNetworkList: NetworkList(emptySet()) {
    override fun permits(address: InetAddress): Boolean = true
}