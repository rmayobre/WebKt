package http.router

import http.route.Route
import http.session.HttpSession

class Router {
    private val routes: MutableMap<String, Route> = mutableMapOf()

    fun inRoute(session: HttpSession): Boolean {
//        if (routes[route.path] == null) {
//            return false
//        }
    }

    fun register(route: Route): Boolean {
        if (routes[route.path] != null) {
            return false
        }
        routes[route.path] = route
        return true
    }
}