package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class CreateLobbyTemplate(
    private val path: String
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        div(classes = "centered") {
            form(action = "${path}create", method = FormMethod.post) {
                submitInput(classes = "form-button centered centered-text") {
                    value = "Create New Lobby"
                }
            }
        }
        br()
    }
}
