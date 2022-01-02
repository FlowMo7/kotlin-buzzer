package dev.moetz.buzzer.plugins

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureStatic() {
    routing {

        route("static") {
            resource(remotePath = "websocket.js", resource = "websocket.js")
            resource(remotePath = "script.js", resource = "script.js")
            resource(remotePath = "reconnecting-websocket.js", resource = "reconnecting-websocket.js")
            resource(remotePath = "reconnecting-websocket.min.js", resource = "reconnecting-websocket.min.js")
            resource(remotePath = "styles.css", resource = "styles.css")

            route("font") {
                resource(remotePath = "Roboto-Bold.ttf", resource = "font/Roboto-Bold.ttf")
            }
        }

    }
}
