package dev.moetz.buzzer.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureMeta(
    formButtonColor: String,
) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Allow: /"
            }
        }

        get("site.webmanifest") {
            call.respondText(contentType = ContentType.parse("application/manifest+json")) {
                """{"name":"Buzzer","short_name":"Buzzer","icons":[{"src":"/icon/android-chrome-192x192.png","sizes":"192x192","type":"image/png"},{"src":"/icon/android-chrome-512x512.png","sizes":"512x512","type":"image/png"}],"theme_color":"$formButtonColor","background_color":"#ffffff","display":"standalone"}"""
            }
        }

        get("status") {
            // any problems would arise at startup anyways, so once the webserver is alive, we are good to go to say
            // we are healthy
            call.respond(HttpStatusCode.NoContent)
        }

    }
}
