package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class SiteTemplate(
    private val siteTitle: String,
    private val description: String = "Online buzzing system for party games.",
    private val formButtonColor: String,
    private val buzzerButtonColorReady: String,
    private val buzzerButtonColorBuzzed: String,
    private val path: String,
) : Template<HTML> {

    val content = Placeholder<FlowContent>()
    val footerContent = Placeholder<FlowContent>()

    val additionalHeadStuff = Placeholder<HEAD>()

    override fun HTML.apply() {
        comment(" This service is open sourced at https://github.com/FlowMo7/kotlin-buzzer. ")
        head {
            meta(charset = "utf-8")
            title { +siteTitle }
            script(type = "text/javascript", src = "${path}js/reconnecting-websocket.min.js") {

            }
            script(type = "text/javascript", src = "${path}js/websocket.js") {

            }
            script(type = "text/javascript", src = "${path}js/script.js") {

            }

            style {
                unsafe {
                    +":root {"
                    +"--button-color: $formButtonColor;"
                    +"--button-buzzer-ready: $buzzerButtonColorReady;"
                    +"--button-buzzer-buzzed: $buzzerButtonColorBuzzed;"
                    +"}"
                }
            }

            link(href = "${path}style/styles.css", rel = "stylesheet", type = "text/css")

            meta(name = "robots", content = "index, follow")
            meta(name = "og:title", content = siteTitle)
            meta(name = "description", content = description)
            meta(name = "keywords", content = "buzzer,buzzing,online,party game")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")

            link(href = "${path}icon/apple-touch-icon.png", rel = "apple-touch-icon") { sizes = "180x180" }
            link(href = "${path}icon/favicon-32x32.png", type = "image/png", rel = "icon") { sizes = "32x32" }
            link(href = "${path}icon/favicon-16x16.png", type = "image/png", rel = "icon") { sizes = "16x16" }
            link(href = "${path}site.webmanifest", rel = "manifest")

            insert(additionalHeadStuff)
        }
        body {
            main {
                insert(content)
            }
            footer {
                insert(footerContent)
            }
        }
    }

}
