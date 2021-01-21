package engine.attachment

import channel.NetworkChannel
import java.nio.channels.DatagramChannel
//
//data class DatagramChannelAttachment<T : NetworkChannel<DatagramChannel>>(
//    override val channel: T,
//    override val storage: Any? = null
//) : Attachment<DatagramChannel>