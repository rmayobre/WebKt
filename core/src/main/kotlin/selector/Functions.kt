package selector

import java.nio.channels.SelectionKey

/**
 * Register a SelectableChannel to accept incoming connection requests.
 * @see SelectionKey.OP_ACCEPT
 */
const val ACCEPT_OPERATION = SelectionKey.OP_ACCEPT

/**
 * Register a SelectableChannel to connect to a remote connection.
 * @see SelectionKey.OP_CONNECT
 */
const val CONNECT_OPERATION = SelectionKey.OP_CONNECT

/**
 * Register a SelectableChannel to read incoming data.
 * @see SelectionKey.OP_READ
 */
const val READ_OPERATION = SelectionKey.OP_READ

/**
 * Register a SelectableChannel to write data to endpoint.
 * @see SelectionKey.OP_WRITE
 */
const val WRITE_OPERATION = SelectionKey.OP_WRITE