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
            val lobbyIdQueryParameter = call.request.queryParameters["lobbyCode"]
            call.respondHtml {
                head {
                    meta(charset = "utf-8")
                    title("Buzzer")
                    script(type = "text/javascript", src = "/static/websocket.js") {

                    }
                    link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
                }
                body {
                    main {
                        div(classes = "header") {
                            h3 { +"Buzzer" }
                        }

                        if (lobbyIdQueryParameter.isNullOrBlank()) {
                            div {
                                form(action = "/create", method = FormMethod.post) {
                                    +"Create new lobby"
                                    submitInput()
                                }
                            }

                            br()
                            br()
                            br()
                        }

                        div {
                            form(action = "/join", method = FormMethod.post) {

                                if (lobbyIdQueryParameter.isNullOrBlank()) {
                                    +"Join a Lobby:"
                                    br()
                                    textInput(name = "lobbyCode") {
                                        placeholder = "Lobby-Code"
                                    }
                                    br()
                                    textInput(name = "nickname") {
                                        placeholder = "Nickname"
                                    }
                                    submitInput()
                                } else {
                                    +"Enter a nickname to join the lobby:"
                                    br()
                                    textInput(name = "nickname") {
                                        placeholder = "Nickname"
                                    }

                                    hiddenInput(name = "lobbyCode") {
                                        value = lobbyIdQueryParameter
                                    }

                                    submitInput()
                                }
                            }
                        }
                    }
                }
            }
        }

        post("create") {
            val lobbyCode = buzzingSessionManager.createNewLobby()
            call.respondRedirect(url = "/host/$lobbyCode", permanent = false)
        }

        post("join") {
            val (lobbyCode, nickname) = try {
                val parameters = call.receiveParameters()
                parameters["lobbyCode"]!! to parameters["nickname"]
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                null to null
            }
            if (lobbyCode != null) {
                call.respondRedirect(url = "/lobby/$lobbyCode?nickname=$nickname", permanent = false)
            } else {
                call.respondRedirect(url = "/", permanent = false)
            }
        }

        get("lobby/{lobbyId}") {
            val lobbyId = requireNotNull(call.parameters["lobbyId"]) { "lobbyId not set in path" }
            val nickname =
                requireNotNull(call.request.queryParameters["nickname"]) { "nickname not set in query parameters" }

            call.respondHtml {
                head {
                    meta(charset = "utf-8")
                    title("Buzzer")
                    script(type = "text/javascript", src = "/static/websocket.js") {
                    }

                    script(type = "text/javascript") {
                        +"window.onload = function() { participant('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyId'); };"
                    }
                    link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
                }
                body {
                    h3 { +"Buzzer (Participant)" }
                    br()

                    span {
                        id = "connection_status"
                        style = "color: red;"
                    }
                    br()

                    p {
                        +"Your nickname:"
                        unsafe { +"&nbsp;" }
                        i {
                            +nickname
                        }
                    }

                    div {
                        button(classes = "buzzer") {
                            onClick = "sendBuzz();"
                            +"Buzz"
                        }
                    }
                }
            }
        }

        get("host/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyId not set in path" }
            call.respondHtml {
                head {
                    meta(charset = "utf-8")
                    title("Buzzer Host")
                    script(type = "text/javascript", src = "/static/websocket.js") {

                    }

                    script(type = "text/javascript") {
                        +"window.onload = function() { host('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyCode'); };"
                    }
                    link(href = "/static/styles.css", rel = "stylesheet", type = "text/css")
                }
                body {
                    h3 { +"Buzzer (Host)" }
                    br()

                    span {
                        id = "connection_status"
                        style = "color: red;"
                    }

                    div {
                        id = "lobby_code"
                        +"Lobby-Code: $lobbyCode"
                    }

                    div {
                        +"Link to join this lobby:"
                        br()
                        val lobbyJoinUrl = buildString {
                            if (isSecure) {
                                append("https://")
                            } else {
                                append("http://")
                            }
                            append(publicHostname)
                            append("/?lobbyCode=$lobbyCode")
                        }
                        a(href = lobbyJoinUrl) { +lobbyJoinUrl }
                    }
                    br()

                    div {
                        id = "participant_list"
                    }

                    br()
                    br()
                    div {
                        button {
                            onClick = "resetBuzzes();"
                            +"Reset Buzzes"
                        }
                    }
                }
            }
        }

    }
}
