package engine.deprecated

import java.nio.channels.SelectionKey

/**
 * Available operations to register a SelectableChannel to a SelectorRunnable.
 */
@Deprecated("remove")
enum class Operation(val flag: Int) {
    /**
     * Register a SelectableChannel to accept incoming connection requests.
     * @see SelectionKey.OP_ACCEPT
     */
    ACCEPT(SelectionKey.OP_ACCEPT),

    /**
     * Register a SelectableChannel to connect to a remote connection.
     * @see SelectionKey.OP_CONNECT
     */
    CONNECT(SelectionKey.OP_CONNECT),

    /**
     * Register a SelectableChannel to read incoming data.
     * @see SelectionKey.OP_READ
     */
    READ(SelectionKey.OP_READ),

    /**
     * Register a SelectableChannel to write data to endpoint.
     * @see SelectionKey.OP_WRITE
     */
    WRITE(SelectionKey.OP_WRITE)
}