package engine


///**
// * Registers a NetworkChannel to the Selector. Registry process works by registering
// * the NetworkChannel's SelectableChannel with an Attachment class that wraps the
// * NetworkChannel and the provided Attachment. To get the NetworkChannel, during a
// * Selector's select (select, selectNow, etc), the NetworkChannel will be provided
// * within the [SelectionKey.attachment]
// */
//fun Selector.register(
//    networkChannel: SuspendedNetworkChannel<*>,
//    operationFlag: Int,
//    attachment: Any? = null
//) {
//    val channel = networkChannel.channel
//    if (isOpen && networkChannel.channel.isOpen) {
//        channel.register(
//            this,
//
//        )
//        register(
//            channel = networkChannel.channel,
//            operation = operationFlag,
//            attachment = Attachment(
//                channel = networkChannel,
//                storage = attachment
//            )
//        )
//    }
//}
//
///**
// * Register channel back into selector. Only registers channel if channel is open.
// * @param channel SelectableChannel to be registered to Selector.
// * @param operation Operation the Channel will be registered to perform. NOTE, you can register multiple operations at the same time.
// * @param attachment An attachment to be provided for the channel's next operation.
// */
//fun Selector.register(
//    channel: SelectableChannel,
//    operation: Int,
//    attachment: Any? = null
//) {
//    if (isOpen && channel.isOpen) {
//        channel.register(this, operation, attachment)
//    }
//}