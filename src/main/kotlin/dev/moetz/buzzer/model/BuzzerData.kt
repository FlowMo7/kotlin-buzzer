package dev.moetz.buzzer.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class BuzzerData(
    val id: String,
    val participants: List<ParticipantState>
) {

    @Serializable
    data class ParticipantState(
        val index: Int,
        val name: String,
        val buzzed: Boolean,
        @Serializable(OffsetDateTimeSerializer::class)
        val buzzedAt: OffsetDateTime? = null
    )

}

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val string = decoder.decodeString()
        return OffsetDateTime.parse(string, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
