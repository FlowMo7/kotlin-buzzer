package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import dev.moetz.buzzer.template.CreateLobbyTemplate
import dev.moetz.buzzer.template.JoinLobbyTemplate
import dev.moetz.buzzer.template.SiteTemplate
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
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
            call.respondHtmlTemplate(SiteTemplate()) {
                siteTitle { +"Buzzer" }

                content {
                    div(classes = "header") {
                        h2 { +"Buzzer" }
                    }

                    insert(CreateLobbyTemplate()) {

                    }

                    insert(JoinLobbyTemplate(lobbyCode = null)) {

                    }
                }
            }
        }

        get("status") {
            // any problems would arise at startup anyways, so once the webserver is alive, we are good to go to say
            // we are healthy
            call.respond(HttpStatusCode.NoContent)
        }

        post("create") {
            val lobbyCode = buzzingSessionManager.createNewLobby()
            call.respondRedirect(url = "/host/$lobbyCode", permanent = false)
        }

        route("join") {

            get("{lobbyCode}") {
                val lobbyCode = call.parameters["lobbyCode"]?.ifBlank { null }
                if (lobbyCode == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
                } else {
                    call.respondHtmlTemplate(SiteTemplate()) {
                        siteTitle { +"Buzzer" }

                        content {
                            div(classes = "header") {
                                h2 { +"Buzzer" }
                            }

                            insert(JoinLobbyTemplate(lobbyCode = lobbyCode)) {

                            }
                        }
                    }
                }
            }

            post {
                val (lobbyCode, nickname) = try {
                    val parameters = call.receiveParameters()
                    parameters["lobbyCode"]?.ifBlank { null } to parameters["nickname"]?.ifBlank { null }
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    null to null
                }
                if (lobbyCode != null && nickname != null && buzzingSessionManager.isValidLobbyCode(lobbyCode)) {
                    call.respondRedirect(url = "/lobby/$lobbyCode?nickname=$nickname", permanent = false)
                } else {
                    call.respondRedirect(url = "/", permanent = false)
                }
            }
        }

        get("lobby/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyId not set in path" }
            val nickname =
                requireNotNull(call.request.queryParameters["nickname"]) { "nickname not set in query parameters" }

            if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
            } else {
                call.respondHtmlTemplate(SiteTemplate()) {
                    siteTitle { +"Buzzer" }

                    additionalHeadStuff {
                        script(type = "text/javascript") {
                            +"window.onload = function() { participant('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyCode'); };"
                        }
                    }

                    content {
                        div(classes = "header") {
                            h3 { +"Buzzer" }
                        }

                        div(classes = "centered centered-text") {
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
                                    id = "buzzer_button"
                                    onClick = "sendBuzz();"
                                    +"Buzz"
                                }
                            }
                        }
                    }
                }
            }
        }

        get("host/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyId not set in path" }

            if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
            } else {
                call.respondHtmlTemplate(SiteTemplate()) {
                    siteTitle { +"Buzzer Host" }

                    additionalHeadStuff {
                        script(type = "text/javascript") {
                            +"window.onload = function() { host('${if (isSecure) "wss" else "ws"}://${publicHostname}', '$lobbyCode'); };"
                        }
                    }

                    content {
                        div(classes = "header") {
                            h3 { +"Buzzer (Host)" }
                        }
                        div(classes = "centered centered-text") {

                            span {
                                id = "connection_status"
                                style = "color: red;"
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
                                    append("/join/$lobbyCode")
                                }
                                a(href = lobbyJoinUrl) { +lobbyJoinUrl }
                            }

                            div {
                                button(classes = "form-button centered-text centered") {
                                    style = "background-color: dimgray;"
                                    onClick = "resetBuzzes();"
                                    +"Reset Buzzes"
                                }
                            }

                            div(classes = "participant-box") {
                                div("centered-text") { +"Buzzes:" }
                                br()
                                div {
                                    id = "buzzes_list"
                                }
                            }

                            br()

                            div(classes = "participant-box") {
                                +"List of participants"
                                unsafe { +"&nbsp;(" }
                                span {
                                    id = "participant_count"
                                    +"0"
                                }
                                +"):"
                                div {
                                    id = "participant_list"
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
