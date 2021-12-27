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


fun Application.configureWebSocket(json: Json, buzzingSessionManager: BuzzingSessionManager) {
    routing {
        route("ws") {
            webSocket("feed/{id}") {
                val id = requireNotNull(call.parameters["id"]) { "path parameter {id} not set" }
                buzzingSessionManager.getBuzzerFlow(id = id)
                    .onEach { buzzingSessionData ->
                        println("received a data update from buzzingSessionManager: $buzzingSessionData")

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
                            println("received text-frame: $receivedText")
                            val incomingMessage = json.decodeFromString(IncomingMessage.serializer(), receivedText)
                            when (incomingMessage.type) {
                                IncomingMessage.Type.Buzz -> {
                                    buzzingSessionManager.addBuzz(
                                        id = incomingMessage.lobbyCode,
                                        participantName = requireNotNull(incomingMessage.participant) { "participant" }
                                    )
                                }
                                IncomingMessage.Type.Clear -> {
                                    buzzingSessionManager.clearBuzzes(
                                        id = incomingMessage.lobbyCode
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
