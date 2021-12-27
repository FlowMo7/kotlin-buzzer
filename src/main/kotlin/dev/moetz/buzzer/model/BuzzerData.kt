package dev.moetz.buzzer.model

import kotlinx.serialization.Serializable

@Serializable
data class BuzzerData(
    val id: String,
    val participants: List<ParticipantState>
) {
    
    @Serializable
    data class ParticipantState(
        val index: Int,
        val name: String,
        val buzzed: Boolean
    )
    
}
