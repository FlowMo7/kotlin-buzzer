package dev.moetz.buzzer

import dev.moetz.buzzer.manager.BuzzLogging
import dev.moetz.buzzer.manager.BuzzingSessionManager
import dev.moetz.buzzer.plugins.configure
import dev.moetz.buzzer.plugins.configureStatic
import dev.moetz.buzzer.plugins.configureWebSocket
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Duration

fun main() {

    val isSecure = System.getenv("IS_SECURE")?.takeIf { it.isNotBlank() }?.toBooleanStrict() ?: false
    val publicHostname = System.getenv("PUBLIC_HOSTNAME")?.takeIf { it.isNotBlank() } ?: "localhost:8080"
    val debugLogsEnabled = System.getenv("ENABLE_DEBUG_LOGS")?.takeIf { it.isNotBlank() }?.toBooleanStrict() ?: true
    val logFilePath = "/etc/log/kotlin-buzzer"


    val buzzLogging = BuzzLogging(
        logFile = File(logFilePath, "log.txt"),
        alsoLogToStdout = debugLogsEnabled
    )

    val buzzingSessionManager = BuzzingSessionManager(
        buzzLogging = buzzLogging
    )

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
        }
        install(DefaultHeaders)
        install(AutoHeadResponse)
        install(CachingHeaders) {
            options {
                CachingOptions(CacheControl.NoCache(CacheControl.Visibility.Public))
            }
        }
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
        }

        configure(buzzingSessionManager, publicHostname, isSecure)
        configureStatic()
        configureWebSocket(Json, buzzingSessionManager)
    }.start(wait = true)
}
