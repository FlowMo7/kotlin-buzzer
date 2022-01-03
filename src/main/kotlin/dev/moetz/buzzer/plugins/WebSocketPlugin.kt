package dev.moetz.buzzer.plugins

import dev.moetz.buzzer.manager.BuzzingSessionManager
import dev.moetz.buzzer.model.BuzzerData
import dev.moetz.buzzer.model.IncomingMessage
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json


fun Application.configureWebSocket(
    json: Json,
    buzzingSessionManager: BuzzingSessionManager,
    debugEnabled: Boolean
) {
    routing {
        route("ws") {

            webSocket("feed/{id}") {

                val id = requireNotNull(call.parameters["id"]) { "path parameter {id} not set" }
                val nickname =
                    requireNotNull(call.request.queryParameters["nickname"]) { "query parameter {nickname} not set" }

                buzzingSessionManager.onParticipantEntered(id = id, nickname = nickname)

                buzzingSessionManager.getBuzzerFlow(id = id)
                    .onEach { buzzingSessionData ->
                        val apiModel = BuzzerData(
                            id = buzzingSessionData.id,
                            participants = buzzingSessionData.participantsState.map { participantState ->
                                BuzzerData.ParticipantState(
                                    index = participantState.index,
                                    name = participantState.name,
                                    buzzed = participantState.buzzed
                                )
                            }
                        )
                        send(json.encodeToString(BuzzerData.serializer(), apiModel))
                    }
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
                                        id = id,
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
                if (debugEnabled) {
                    println("Session closed for lobby $id and nickname $nickname.")
                    println("Closed reason: ${closeReason.await()}")
                }
                buzzingSessionManager.onParticipantLeft(id = id, nickname = nickname)
            }

            webSocket("host/{id}") {

                val id = requireNotNull(call.parameters["id"]) { "path parameter {id} not set" }

                buzzingSessionManager.onHostEntered(id = id)

                buzzingSessionManager.getBuzzerFlow(id = id)
                    .onEach { buzzingSessionData ->
                        val apiModel = BuzzerData(
                            id = buzzingSessionData.id,
                            participants = buzzingSessionData.participantsState.map { participantState ->
                                BuzzerData.ParticipantState(
                                    index = participantState.index,
                                    name = participantState.name,
                                    buzzed = participantState.buzzed
                                )
                            }
                        )
                        send(json.encodeToString(BuzzerData.serializer(), apiModel))
                    }
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
                                        id = id
                                    )
                                }
                                IncomingMessage.Type.Buzz -> {
                                    //Hosts are not allowed to buzz
                                }
                            }
                        }
                    }
                }
                if (debugEnabled) {
                    println("Session closed for lobby $id as host.")
                    println("Closed reason: ${closeReason.await()}")
                }
                buzzingSessionManager.onHostLeft(id = id)
            }

        }

    }
}
