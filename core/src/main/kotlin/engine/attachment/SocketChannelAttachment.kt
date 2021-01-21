package engine.attachment

import channel.NetworkChannel
import java.nio.channels.SocketChannel

//data class SocketChannelAttachment<T : NetworkChannel<SocketChannel>>(
//    override val channel: T,
//    override val storage: Any? = null
//) : Attachment<SocketChannel>