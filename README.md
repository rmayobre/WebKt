# WebKt
An extremely lightweight networking library that only uses native java libraries (also native Kotlin libraries). This library supports HTTP, as well as Websockets, and follows [RFC 6455](https://tools.ietf.org/html/rfc6455) guidelines. This library is dedicated to serving as a lightweight server or client networking library for smaller systems. A background server process (such as Apache Tomcat or Jetty) will not be needed as this library will do all the HTTP processing for you (if you wish). WebKt is designed with choice in mind. A lot of the classes and interfaces are designed with the idea for the developer to take what they like, and make the rest. All parts of this library are modular, and all code is licensed to remain open-sourced.

# Current State
WebKt is still in a alpha/beta state, and many of the APIs and overall functionality can change dramatically before entering release state, version 1.0.0. To speed up any of the process, input is always appreciated and bug reports help. To get a feel as to what is currently available and working, check out the `example` module in the source files. I hope to also have a detailed wiki for this library soon, so long as the library doesn't require anymore dramatic changes.

# Features
* Supports Java 8 (anything later has not be tested yet).
* Non-blocking channels for server-side implementations.
* SSL SocketChannels - beta
* SSL ServerSocketChannel - beta
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
* HTTPS support
* WSS support
* Wiki page - Detailed instructions on how to use WebKt

# Dependencies
* Java 8 
* Kotlin 1.3
