package dev.moetz.buzzer.model

import kotlinx.serialization.Serializable

@Serializable
data class IncomingMessage(
    val type: Type
) {
    enum class Type {
        Buzz, Clear
    }
}
