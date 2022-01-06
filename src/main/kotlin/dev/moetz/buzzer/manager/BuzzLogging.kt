package dev.moetz.buzzer.manager

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class BuzzLogging(
    private val enabled: Boolean = true
) {

    private val dateTimeFormat: DateTimeFormatter by lazy {
        DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .appendLiteral('T')
            .appendPattern("HH:mm:ss.SSSX")
            .toFormatter()
    }

    enum class Role {
        Participant, Host
    }

    fun log(lobby: String, role: Role, message: String) {
        if (enabled) {
            val dateTimeString = OffsetDateTime.now()
                .atZoneSameInstant(ZoneId.of("UTC"))
                .format(dateTimeFormat)
            val logLine = dateTimeString + " #$lobby [${role.name}] $message"
            println(logLine)
        }
    }

}