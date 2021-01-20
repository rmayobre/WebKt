import kotlinx.coroutines.CoroutineScope
import java.nio.channels.SelectionKey

interface NetworkApplication {

    val appScope: CoroutineScope

    fun onValidKey(key: SelectionKey)
}