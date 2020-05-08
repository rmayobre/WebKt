# WebKt
Websocket implementation for Kotlin following [RFC 6455](https://tools.ietf.org/html/rfc6455). This library allows Kotlin/JVM projects to utilize the Websocket protocol on the client and server-side. Server-side implementations utilize Java's NIO libraries, while the client-side Websockets are IO (running two threads for reads and writes).

# Features
* Supports Java 8 (currently does not support Android encoding, but will in an upcoming update).
* Non-blocking channels for server=side implementations.
* HTTP Support
    * Server-side request reading
    * Server-side responses
    * Paths (An object that handles a paths requests and methods).
    * Blacklist network addresses.
    * Whitelist network addresses.
    * HttpExceptions
    
* Websocket Support
    * Server-side support
    * Client-side support
    * Frame encoding/decoding
    * Buffer and Stream reading support
    * Handshake Requests
    * Custom Hanshake headers
    * Ping/Pong
    * Frame Fragmentation
 
* Android Support (Not fully tested)
    * Base 64 encoding support

# Future Features (Check project boards for updates)
* SSL SocketChannels
* HTTPS support
* WSS support
* Wiki page - Detailed instructions on how to use WebKt

# Dependencies
Java 8 - Everything is written with native libraries (with Kotlin)
