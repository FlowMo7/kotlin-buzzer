package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class InfoSiteItem : Template<FlowContent> {

    val titlePlaceholder = Placeholder<H4>()
    val content = Placeholder<FlowContent>()

    override fun FlowContent.apply() {
        div(classes = "centered") {
            h4 { insert(titlePlaceholder) }

            div {
                insert(content)
            }
        }
        br()
    }
}