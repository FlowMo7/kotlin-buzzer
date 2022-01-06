package dev.moetz.buzzer.plugins

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*


fun Application.configureStatic() {
    routing {

        route("js") {
            resource(remotePath = "websocket.js", resource = "websocket.js")
            resource(remotePath = "script.js", resource = "script.js")
            resource(remotePath = "reconnecting-websocket.js", resource = "reconnecting-websocket.js")
            resource(remotePath = "reconnecting-websocket.min.js", resource = "reconnecting-websocket.min.js")
        }

        route("style") {
            resource(remotePath = "styles.css", resource = "styles.css")

            route("font") {
                resource(remotePath = "Roboto-Bold.ttf", resource = "font/Roboto-Bold.ttf")
            }
        }

        route("icon") {
            resource(remotePath = "android-chrome-192x192.png", resource = "icon/android-chrome-192x192.png")
            resource(remotePath = "android-chrome-512x512.png", resource = "icon/android-chrome-512x512.png")
            resource(remotePath = "apple-touch-icon.png", resource = "icon/apple-touch-icon.png")
            resource(remotePath = "favicon.ico", resource = "icon/favicon.ico")
            resource(remotePath = "favicon-16x16.png", resource = "icon/favicon-16x16.png")
            resource(remotePath = "favicon-32x32.png", resource = "icon/favicon-32x32.png")
        }

    }
}
