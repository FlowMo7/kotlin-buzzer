package dev.moetz.buzzer.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Webmanifest(
    val name: String,
    @SerialName("short_name")
    val shortName: String,
    val icons: List<Icon>,
    @SerialName("theme_color")
    val themeColor: String,
    @SerialName("background_color")
    val backgroundColor: String,
    val display: String
) {
    @Serializable
    data class Icon(
        val src: String,
        val sizes: String,
        val type: String
    )
}

fun Application.configureMeta(
    formButtonColor: String,
    path: String,
) {

    routing {

        get("robots.txt") {
            call.respondText(ContentType.Text.Plain) {
                "User-agent: * Allow: /"
            }
        }

        get("site.webmanifest") {

            call.respond(
                Webmanifest(
                    name = "Buzzer",
                    shortName = "Buzzer",
                    icons = listOf(
                        Webmanifest.Icon(
                            src = "${path}icon/android-chrome-192x192.png",
                            sizes = "192x192",
                            type = "image/png"
                        ),
                        Webmanifest.Icon(
                            src = "${path}icon/android-chrome-512x512.png",
                            sizes = "512x512",
                            type = "image/png"
                        )
                    ),
                    themeColor = formButtonColor,
                    backgroundColor = "#ffffff",
                    display = "standalone"
                )
            )
        }

        get("status") {
            // any problems would arise at startup anyways, so once the webserver is alive, we are good to go to say
            // we are healthy
            call.respond(HttpStatusCode.NoContent)
        }

    }
}
