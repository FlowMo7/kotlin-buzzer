package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

class SiteTemplate : Template<HTML> {

    val siteTitle = Placeholder<TITLE>()
    val content = Placeholder<FlowContent>()

    val additionalHeadStuff = Placeholder<HEAD>()

    override fun HTML.apply() {
        head {
            meta(charset = "utf-8")
            title { insert(siteTitle) }
            script(type = "text/javascript", src = "/static/websocket.js") {

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


class CreateLobbyTemplate : Template<FlowContent> {
    override fun FlowContent.apply() {
        div(classes = "centered") {
            form(action = "/create", method = FormMethod.post) {
                submitInput(classes = "form-button centered centered-text") {
                    value = "Create New Lobby"
                }
            }
        }
        br()
    }
}

class JoinLobbyTemplate(
    private val lobbyCode: String?
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        if (lobbyCode == null) {
            div(classes = "centered") {
                form(action = "/join", method = FormMethod.post) {
                    span(classes = "centered-text") { +"Join a Lobby:" }
                    br()
                    textInput(name = "lobbyCode", classes = "form-input") {
                        required = true
                        placeholder = "Lobby-Code"
                    }
                    textInput(name = "nickname", classes = "form-input") {
                        required = true
                        placeholder = "Nickname"
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
                    textInput(name = "nickname", classes = "form-input") {
                        required = true
                        placeholder = "Nickname"
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

        post("create") {
            val lobbyCode = buzzingSessionManager.createNewLobby()
            call.respondRedirect(url = "/host/$lobbyCode", permanent = false)
        }

        route("join") {

            get("{lobbyCode}") {
                val lobbyCode = call.parameters["lobbyCode"]?.ifBlank { null }
                if (lobbyCode == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respondHtmlTemplate(SiteTemplate()) {
                        siteTitle {
                            +"Buzzer"
                        }

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
                if (lobbyCode != null && nickname != null) {
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

        get("host/{lobbyCode}") {
            val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "lobbyId not set in path" }


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
                                style = "background-color: darkred;"
                                onClick = "resetBuzzes();"
                                +"Reset Buzzes"
                            }
                        }

                        div(classes = "participant-box") {
                            div("centered-text") { +"Buzzes:" }
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
