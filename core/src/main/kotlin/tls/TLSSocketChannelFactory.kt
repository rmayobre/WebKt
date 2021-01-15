package tls

import java.nio.channels.SelectableChannel

interface TLSSocketChannelFactory<T : SelectableChannel> {
    fun create(channel: T): TLSChannel
}