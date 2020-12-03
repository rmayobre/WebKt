//typealias httpEngine = HttpEngineBuilder.httpEngine

//val httpEngine: (HttpEngineBuilder.() -> Unit) -> HttpEngine = HttpEngineBuilder.Companion::httpEngineBuilder
//
//class HttpEngineBuilder internal constructor() {
//
//    private fun build(): HttpEngine = object : HttpEngine() {
//        override val channelFactory: MessageChannelFactory
//            get() = TODO("Not yet implemented")
//        override val sessionFactory: HttpSessionFactory<SocketChannel>
//            get() = TODO("Not yet implemented")
//
//        override fun onMessage(message: Message) {
//
//            val e: HttpEngine = httpEngine {
//
//            }
//            TODO("Not yet implemented")
//        }
//
//    }
//
//    companion object {
//        internal fun httpEngineBuilder(builderFunc: HttpEngineBuilder.() -> Unit): HttpEngine {
//            val builder = HttpEngineBuilder()
//            builderFunc(builder)
//            return builder.build()
//        }
//    }
//}