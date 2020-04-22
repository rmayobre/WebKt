# WebKt
Websocket implementation for Kotlin following [RFC 6455](https://tools.ietf.org/html/rfc6455). This library allows Kotlin/JVM projects to utilize the Websocket protocol on the client and server-side. Server-side implementations utilize Java's NIO libraries, while the client-side Websockets are IO (running two threads for reads and writes).

# Features
* Supports Java 8 (currently does not support Android encoding, but will in an upcoming update).
* Frame encoding/decoding
* Buffer and Stream reading support
* Handshake Requests
* Custom Hanshake headers
* Ping/Pong
* Frame Fragmentation

# Future Features
* Android support - Handshake encoding does not work at the moment.
* WSS support
* Wiki page - Detailed instructions on how to use WebKt

# Dependencies
Java 8 - Everything is written with native libraries
