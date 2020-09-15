package http.router

import http.route.Route
import http.session.Session

class Router {
    private val routes: MutableMap<String, Route> = mutableMapOf()

    fun inRoute(session: Session): Boolean {
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