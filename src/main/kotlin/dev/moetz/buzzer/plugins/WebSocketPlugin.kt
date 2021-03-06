package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import dev.moetz.buzzer.manager.ConnectionCountManager
import dev.moetz.buzzer.model.BuzzerData
import dev.moetz.buzzer.model.IncomingMessage
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

private fun BuzzingSessionManager.BuzzingSessionData.toBuzzerData(): BuzzerData {
    return BuzzerData(
        id = this.id,
        participants = this.participantsState.map { participantState ->
            BuzzerData.ParticipantState(
                index = participantState.index,
                name = participantState.name.preventXSS(),
                buzzed = participantState.buzzed,
                buzzedAt = participantState.buzzedAt
            )
        }
    )
}

fun Application.configureWebSocket(
    json: Json,
    buzzingSessionManager: BuzzingSessionManager,
    connectionCountManager: ConnectionCountManager
) {
    routing {

        route("ws") {

            webSocket("feed/{lobbyCode}") {
                val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "path parameter {lobbyCode} not set" }
                val nickname =
                    requireNotNull(call.request.queryParameters["nickname"]) { "query parameter {nickname} not set" }

                if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "lobbyCode contains invalid characters"))
                } else if (buzzingSessionManager.isValidNickname(nickname).not()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "nickname contains invalid characters"))
                } else {
                    connectionCountManager.onParticipantConnected(lobbyCode = lobbyCode)
                    buzzingSessionManager.onParticipantEntered(id = lobbyCode, nickname = nickname)

                    buzzingSessionManager.getBuzzerFlow(id = lobbyCode)
                        .map { it.toBuzzerData() }
                        .onEach { buzzerData -> send(json.encodeToString(BuzzerData.serializer(), buzzerData)) }
                        .flowOn(Dispatchers.IO)
                        .launchIn(this)

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                val incomingMessage = json.decodeFromString(IncomingMessage.serializer(), receivedText)
                                when (incomingMessage.type) {
                                    IncomingMessage.Type.Buzz -> {
                                        buzzingSessionManager.addBuzz(
                                            id = lobbyCode,
                                            participantName = nickname
                                        )
                                    }
                                    IncomingMessage.Type.Clear -> {
                                        //participants are not allowed to clear
                                    }
                                }
                            }
                        }
                    }
                    buzzingSessionManager.onParticipantLeft(id = lobbyCode, nickname = nickname)
                    connectionCountManager.onParticipantConnectionTerminated(lobbyCode = lobbyCode)
                }
            }

            webSocket("host/{lobbyCode}") {
                val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "path parameter {lobbyCode} not set" }
                val hostSecret = call.request.queryParameters["secret"].orEmpty()

                if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "lobbyCode contains invalid characters"))
                } else if (buzzingSessionManager.verifyHostSecret(id = lobbyCode, secret = hostSecret).not()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid host secret"))
                } else {
                    connectionCountManager.onHostConnected(lobbyCode = lobbyCode)
                    buzzingSessionManager.onHostEntered(id = lobbyCode)

                    buzzingSessionManager.getBuzzerFlow(id = lobbyCode)
                        .map { it.toBuzzerData() }
                        .onEach { buzzerData -> send(json.encodeToString(BuzzerData.serializer(), buzzerData)) }
                        .flowOn(Dispatchers.IO)
                        .launchIn(this)

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                val incomingMessage = json.decodeFromString(IncomingMessage.serializer(), receivedText)
                                when (incomingMessage.type) {
                                    IncomingMessage.Type.Clear -> {
                                        buzzingSessionManager.clearBuzzes(
                                            id = lobbyCode
                                        )
                                    }
                                    IncomingMessage.Type.Buzz -> {
                                        //Hosts are not allowed to buzz
                                    }
                                }
                            }
                        }
                    }
                    buzzingSessionManager.onHostLeft(id = lobbyCode)
                    connectionCountManager.onHostConnectionTerminated(lobbyCode = lobbyCode)
                }
            }

            webSocket("monitor/{lobbyCode}") {
                val lobbyCode = requireNotNull(call.parameters["lobbyCode"]) { "path parameter {lobbyCode} not set" }

                if (buzzingSessionManager.isValidLobbyCode(lobbyCode).not()) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "lobbyCode contains invalid characters"))
                } else {
                    buzzingSessionManager.getBuzzerFlow(id = lobbyCode)
                        .map { it.toBuzzerData() }
                        .onEach { buzzerData -> send(json.encodeToString(BuzzerData.serializer(), buzzerData)) }
                        .flowOn(Dispatchers.IO)
                        .launchIn(this)

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                //we are not receiving commands on monitoring page, but keep the loop here for suspending reasons
                            }
                        }
                    }
                }
            }

        }

    }
}


/**
 * Just a small thing to prevent script injections using names.
 */
private fun String.preventXSS(): String {
    return this
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}