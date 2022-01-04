package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class JoinLobbyTemplate(
    private val lobbyCode: String?
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        if (lobbyCode == null) {
            div(classes = "centered") {
                form(action = "/join", method = FormMethod.post) {
                    br()
                    span(classes = "centered-text") { +"Join a Lobby:" }
                    br()
                    textInput(name = "lobbyCode", classes = "centered form-input") {
                        required = true
                        pattern = "[a-zA-Z0-9-_]+"
                        placeholder = "Lobby-Code"
                    }
                    textInput(name = "nickname", classes = "form-input centered") {
                        required = true
                        placeholder = "Nickname"
                        pattern = "[a-zA-Z0-9-_\\s]+"
                    }
                    submitInput(classes = "form-button centered centered-text") {
                        value = "Join"
                    }

                }
            }
        } else {
            div(classes = "centered") {
                form(action = "/join", method = FormMethod.post) {
                    +"Enter a nickname to join the lobby:"
                    br()
                    textInput(name = "nickname", classes = "form-input centered") {
                        required = true
                        placeholder = "Nickname"
                        pattern = "[a-zA-Z0-9-_\\s]+"
                    }

                    hiddenInput(name = "lobbyCode") {
                        value = lobbyCode
                    }

                    submitInput(classes = "form-button centered centered-text") {
                        value = "Join"
                    }
                }
            }
        }
    }
}
