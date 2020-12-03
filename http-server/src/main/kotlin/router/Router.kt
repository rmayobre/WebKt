package router

import route.Route
import session.Session

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