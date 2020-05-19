package http.route

import http.message.Request
import http.message.Response

class RouteMap {

    private val getMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val putMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val postMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val deleteMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val optionsMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val traceMap: MutableMap<String, (Request) -> Response> = mutableMapOf()
    private val connectMap: MutableMap<String, (Request) -> Response> = mutableMapOf()

    fun get(endpoint: String, predicate: (Request) -> Response) {
        getMap[endpoint] = predicate
    }

    fun put(endpoint: String, predicate: (Request) -> Response) {
        putMap[endpoint] = predicate
    }

    fun post(endpoint: String, predicate: (Request) -> Response) {
        postMap[endpoint] = predicate
    }

    fun delete(endpoint: String, predicate: (Request) -> Response) {
        deleteMap[endpoint] = predicate
    }

    fun options(endpoint: String, predicate: (Request) -> Response) {
        optionsMap[endpoint] = predicate
    }

    fun trace(endpoint: String, predicate: (Request) -> Response) {
        traceMap[endpoint] = predicate
    }

    fun connect(endpoint: String, predicate: (Request) -> Response) {
        connectMap[endpoint] = predicate
    }
}