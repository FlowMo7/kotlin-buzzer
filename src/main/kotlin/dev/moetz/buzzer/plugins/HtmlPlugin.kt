package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import dev.moetz.buzzer.template.CreateLobbyTemplate
import dev.moetz.buzzer.template.InfoSiteTemplate
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
    path: String,
    isSecure: Boolean,
    formButtonColor: String,
    buzzerButtonColorReady: String,
    buzzerButtonColorBuzzed: String
) {

    routing {

        get {
            call.respondHtmlTemplate(
                SiteTemplate(
                    siteTitle = "Buzzer",
                    formButtonColor = formButtonColor,
                    buzzerButtonColorReady = buzzerButtonColorReady,
                    buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                    path = path
                )
            ) {
                content {
                    div(classes = "header") {
                        h2 { +"Buzzer" }
                    }

                    insert(CreateLobbyTemplate(path)) {

                    }

                    insert(JoinLobbyTemplate(lobbyCode = null, path = path)) {

                    }
                }

                footerContent {
                    div(classes = "right-aligned") {
                        a(href = "${path}info") { +"Information" }
                    }
                }
            }
        }

        get("info") {
            call.respondHtmlTemplate(
                SiteTemplate(
                    siteTitle = "Buzzer - Info",
                    formButtonColor = formButtonColor,
                    buzzerButtonColorReady = buzzerButtonColorReady,
                    buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                    path = path
                )
            ) {
                content {
                    div(classes = "header") {
                        h2 { +"Information" }
                    }

                    insert(InfoSiteTemplate()) {

                    }
                }
            }
        }

        post("create") {
            val (lobbyCode, hostSecret) = buzzingSessionManager.createNewLobby()
            call.respondRedirect(url = "${path}host/$lobbyCode?secret=$hostSecret", permanent = false)
        }

        route("join") {

            get {
                call.respondRedirect(url = path, permanent = false)
            }

            get("{lobbyCode}") {
                val lobbyCode = call.parameters["lobbyCode"]?.ifBlank { null }
                if (lobbyCode == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
                } else {
                    call.respondHtmlTemplate(
                        SiteTemplate(
                            siteTitle = "Buzzer",
                            description = "You're invited to join an online buzzing session.",
                            formButtonColor = formButtonColor,
                            buzzerButtonColorReady = buzzerButtonColorReady,
                            buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                            path = path,
                        )
                    ) {
                        content {
                            div(classes = "header") {
                                h2 { +"Join a Buzzing Lobby" }
                            }

                            insert(JoinLobbyTemplate(lobbyCode = lobbyCode, path = path)) {

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
                if (lobbyCode != null && nickname != null && buzzingSessionManager.isValidLobbyCode(lobbyCode) && buzzingSessionManager.isValidNickname(
                        nickname
                    )
                ) {
                    call.respondRedirect(url = "${path}lobby/$lobbyCode?nickname=$nickname", permanent = false)
                } else {
                    call.respondRedirect(url = path, permanent = false)
                }
            }
        }

        get("lobby/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyId not set in path" }
            val nickname =
                requireNotNull(call.request.queryParameters["nickname"]) { "nickname not set in query parameters" }

            if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
            } else if (buzzingSessionManager.isValidNickname(nickname).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid nickname characters")
            } else {
                call.respondHtmlTemplate(
                    SiteTemplate(
                        siteTitle = "Buzzer",
                        formButtonColor = formButtonColor,
                        buzzerButtonColorReady = buzzerButtonColorReady,
                        buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                        path = path
                    )
                ) {
                    additionalHeadStuff {
                        script(type = "text/javascript") {
                            unsafe { +"window.onload = function() { participant('${if (isSecure) "wss" else "ws"}://${publicHostname}${path}', '$lobbyCode'); };" }
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

                                    img(alt = "Buzz", src = "${path}icon/buzzer.svg") {
                                        id = "buzzer_button_image"
                                        this.width = "150px"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        get("host/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyCode not set in path" }
            val hostSecret = call.request.queryParameters["secret"].orEmpty()

            if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
            } else if (buzzingSessionManager.verifyHostSecret(
                    id = lobbyCode,
                    secret = hostSecret,
                    applyIfNoSecretSetYet = true
                ).not()
            ) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid secret")
            } else {
                val lobbyJoinUrl = buildString {
                    if (isSecure) {
                        append("https://")
                    } else {
                        append("http://")
                    }
                    append(publicHostname)
                    append(path)
                    append("join/$lobbyCode")
                }

                call.respondHtmlTemplate(
                    SiteTemplate(
                        siteTitle = "Buzzer Host",
                        formButtonColor = formButtonColor,
                        buzzerButtonColorReady = buzzerButtonColorReady,
                        buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                        path = path
                    )
                ) {
                    additionalHeadStuff {
                        script(type = "text/javascript") {
                            unsafe { +"window.onload = function() { host('${if (isSecure) "wss" else "ws"}://${publicHostname}${path}', '$lobbyCode'); };" }
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
                                +"Link to share to join this lobby:"
                                br()
                                a(href = lobbyJoinUrl, target = "_blank") { +lobbyJoinUrl }
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

                    footerContent {
                        div(classes = "right-aligned") {
                            a(href = "${path}monitor/$lobbyCode", target = "_blank") { +"Buzzes Overlay Link" }
                        }
                    }
                }
            }
        }

        get("monitor/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyCode not set in path" }

            if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid lobbyCode characters")
            } else {
                call.respondHtmlTemplate(
                    SiteTemplate(
                        siteTitle = "Buzzer Monitor",
                        formButtonColor = formButtonColor,
                        buzzerButtonColorReady = buzzerButtonColorReady,
                        buzzerButtonColorBuzzed = buzzerButtonColorBuzzed,
                        path = path
                    )
                ) {
                    additionalHeadStuff {
                        script(type = "text/javascript") {
                            unsafe { +"window.onload = function() { monitor('${if (isSecure) "wss" else "ws"}://${publicHostname}${path}', '$lobbyCode'); };" }
                        }
                    }

                    content {
                        div(classes = "centered centered-text") {

                            span {
                                id = "connection_status"
                                style = "color: red;"
                            }

                            div(classes = "participant-box") {
                                div {
                                    id = "buzzes_list"
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
