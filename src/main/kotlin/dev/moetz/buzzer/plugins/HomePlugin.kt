package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.configure(
    buzzingSessionManager: BuzzingSessionManager,
    publicHostname: String,
    isSecure: Boolean
) {

    routing {

        get {
            call.respondHtml {
                head {
                    meta(charset = "utf-8")
                    title("Buzzer")
                    script(type = "text/javascript", src = "/static/websocket.js") {

                    }
                }
                body {
                    h3 { +"Buzzer" }
                    br()

                    div {
                        form(action = "/create", method = FormMethod.post) {
                            +"Create new lobby"
                            submitInput()
                        }
                    }

                    br()
                    br()
                    br()

                    div {
                        form(action = "/join", method = FormMethod.post) {
                            +"Join a Lobby:"
                            br()
                            textInput(name = "lobbyCode") {

                            }
                            submitInput()
                        }
                    }
                }
            }
        }

        post("create") {
            val lobbyCode = buzzingSessionManager.createNewLobby()
            call.respondRedirect(url = "/lobby/$lobbyCode/admin", permanent = false)
        }

        post("join") {
            val lobbyCode = try {
                val parameters = call.receiveParameters()
                parameters["lobbyCode"]
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                null
            }
            if (lobbyCode != null) {
                call.respondRedirect(url = "/lobby/$lobbyCode", permanent = false)
            } else {
                call.respondRedirect(url = "/", permanent = false)
            }
        }

        route("lobby/{lobbyId}") {

            get {
                val lobbyId = requireNotNull(call.parameters["lobbyId"]) { "lobbyId not set in path" }
                call.respondHtml {
                    head {
                        meta(charset = "utf-8")
                        title("Buzzer")
                        script(type = "text/javascript", src = "/static/websocket.js") {

                        }

                        script(type = "text/javascript") {
                            +"window.onload = function() { participant('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyId'); };"
                        }
                    }
                    body {
                        h3 { +"Buzzer (Participant)" }
                        br()

                        div {
                            +"Lobby: $lobbyId"
                        }
                        br()
                        p {
                            +"Username:"
                            br()
                            textInput() {
                                id = "username"
                            }
                        }
                        br()
                        p {
                            button {
                                onClick = "sendBuzz('${if (isSecure) "wss" else "ws"}://${publicHostname}', document.getElementById('username').value, '$lobbyId');"
                                +"Buzz"
                            }
                        }
                    }
                }
            }

            get("admin") {
                val lobbyId = requireNotNull(call.parameters["lobbyId"]) { "lobbyId not set in path" }
                call.respondHtml {
                    head {
                        meta(charset = "utf-8")
                        title("Buzzer Admin")
                        script(type = "text/javascript", src = "/static/websocket.js") {

                        }

                        script(type = "text/javascript") {
                            +"window.onload = function() { admin('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyId'); };"
                        }
                    }
                    body {
                        h3 { +"Buzzer (Admin)" }
                        br()

                        div {
                            +"Lobby: $lobbyId"
                        }

                        div {
                            id = "participant_list"
                        }

                        br()
                        br()
                        div {
                            button {
                                onClick = "resetBuzzes('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyId');"
                                +"Reset Buzzes"
                            }
                        }
                    }
                }
            }
        }

    }
}
