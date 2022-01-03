package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class SiteTemplate : Template<HTML> {

    val siteTitle = Placeholder<TITLE>()
    val content = Placeholder<FlowContent>()

    val additionalHeadStuff = Placeholder<HEAD>()

    override fun HTML.apply() {
        comment("This service is open sourced at https://github.com/FlowMo7/kotlin-buzzer.")
        head {
            meta(charset = "utf-8")
            title { insert(siteTitle) }
            script(type = "text/javascript", src = "/static/reconnecting-websocket.min.js") {

            }
            script(type = "text/javascript", src = "/static/websocket.js") {

            }
            script(type = "text/javascript", src = "/static/script.js") {

            }
            link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
            insert(additionalHeadStuff)
        }
        body {
            main {
                insert(content)
            }
        }
    }

}
