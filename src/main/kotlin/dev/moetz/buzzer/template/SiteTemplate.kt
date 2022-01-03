package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class SiteTemplate(private val siteTitle: String, private val description: String = "Online buzzing system for party games.") : Template<HTML> {

    val content = Placeholder<FlowContent>()

    val additionalHeadStuff = Placeholder<HEAD>()

    override fun HTML.apply() {
        comment("This service is open sourced at https://github.com/FlowMo7/kotlin-buzzer.")
        head {
            meta(charset = "utf-8")
            title { +siteTitle }
            script(type = "text/javascript", src = "/static/reconnecting-websocket.min.js") {

            }
            script(type = "text/javascript", src = "/static/websocket.js") {

            }
            script(type = "text/javascript", src = "/static/script.js") {

            }
            link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")

            meta(name = "robots", content = "index, follow")
            meta(name = "og:title", content = siteTitle)
            meta(name = "description", content = description)
            meta(name = "keywords", content = "buzzer,buzzing,online,party game")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")

            insert(additionalHeadStuff)
        }
        body {
            main {
                insert(content)
            }
        }
    }

}
