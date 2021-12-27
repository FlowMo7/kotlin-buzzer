package dev.moetz.buzzer.model

import kotlinx.serialization.Serializable

@Serializable
data class IncomingMessage(
    val type: Type,
    val lobbyCode: String,
    val participant: String? = null
) {
    enum class Type {
        Buzz, Clear
    }
}
