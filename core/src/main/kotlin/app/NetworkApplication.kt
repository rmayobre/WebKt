package app

import kotlinx.coroutines.*
import java.nio.channels.SelectionKey

interface NetworkApplication {

    val appScope: CoroutineScope

    /**
     * Called when a [SelectionKey] is ready for an operation.
     */
    fun onValidKey(key: SelectionKey)
}