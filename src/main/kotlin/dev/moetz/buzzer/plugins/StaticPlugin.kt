package dev.moetz.buzzer.plugins

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureStatic() {
    routing {

        route("static") {
            resource(remotePath = "websocket.js", resource = "websocket.js")
            resource(remotePath = "styles.css", resource = "styles.css")
        }

    }
}
