package dev.moetz.buzzer.manager

import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class BuzzLogging(
    private val logFile: File,
    private val alsoLogToStdout: Boolean = true
) {

    init {
        if (logFile.exists().not()) {
            if (logFile.parentFile.exists().not()) {
                if (logFile.parentFile.mkdirs().not()) {
                    throw IllegalStateException("Could not create directories for logfile: ${logFile.parentFile.absolutePath}")
                }
            }
            logFile.createNewFile()
            if (logFile.exists().not()) {
                throw IllegalStateException("Could not create logfile: ${logFile.absolutePath}")
            }
            if (logFile.canWrite().not()) {
                throw IllegalStateException("No write access to logfile: ${logFile.absolutePath}")
            }
        }
    }

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
        val dateTimeString = OffsetDateTime.now()
            .atZoneSameInstant(ZoneId.of("UTC"))
            .format(dateTimeFormat)
        val logLine = dateTimeString + " #$lobby [${role.name}] $message"
        if (alsoLogToStdout) {
            println(logLine)
        }
        logFile.appendText(logLine + "\n")
    }

}