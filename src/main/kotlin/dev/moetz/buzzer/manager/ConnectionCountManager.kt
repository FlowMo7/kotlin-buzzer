package dev.moetz.buzzer.manager

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.OffsetDateTime

class ConnectionCountManager {

    data class ConnectionCounter(
        val lobby: String,
        var numberOfHostConnections: Int,
        var numberOfParticipantConnections: Int,
        var latestHostDisconnect: OffsetDateTime?
    )

    private val activeConnections: MutableList<ConnectionCounter> = mutableListOf()

    private val mutex = Mutex()

    suspend fun onHostConnected(lobbyCode: String) {
        mutex.withLock {
            if (activeConnections.any { it.lobby == lobbyCode }) {
                activeConnections.first { it.lobby == lobbyCode }.apply {
                    numberOfHostConnections += 1
                    latestHostDisconnect = null
                }
            } else {
                activeConnections.add(
                    ConnectionCounter(
                        lobby = lobbyCode,
                        numberOfHostConnections = 1,
                        numberOfParticipantConnections = 0,
                        latestHostDisconnect = null
                    )
                )
            }
        }
    }

    suspend fun onHostConnectionTerminated(lobbyCode: String) {
        mutex.withLock {
            if (activeConnections.any { it.lobby == lobbyCode }) {
                activeConnections.first { it.lobby == lobbyCode }.apply {
                    numberOfHostConnections -= 1
                    if (numberOfHostConnections == 0) {
                        latestHostDisconnect = OffsetDateTime.now()
                    }
                }
            }
        }
    }

    suspend fun onParticipantConnected(lobbyCode: String) {
        mutex.withLock {
            if (activeConnections.any { it.lobby == lobbyCode }) {
                activeConnections.first { it.lobby == lobbyCode }.numberOfParticipantConnections += 1
            } else {
                activeConnections.add(
                    ConnectionCounter(
                        lobby = lobbyCode,
                        numberOfHostConnections = 0,
                        numberOfParticipantConnections = 1,
                        latestHostDisconnect = null
                    )
                )
            }
        }
    }

    suspend fun onParticipantConnectionTerminated(lobbyCode: String) {
        mutex.withLock {
            if (activeConnections.any { it.lobby == lobbyCode }) {
                activeConnections.first { it.lobby == lobbyCode }.numberOfParticipantConnections -= 1
            }
        }
    }

    val cleanUpLobbyFlow: Flow<String> = flow {
        while (currentCoroutineContext().isActive) {
            delay(30_000) //30s
            emit(Unit)
        }
    }
        .flatMapConcat {
            flow {
                mutex.withLock {
                    activeConnections
                        .asSequence()
                        .filter {
                            it.numberOfHostConnections == 0 &&
                                    it.numberOfParticipantConnections == 0 &&
                                    it.latestHostDisconnect?.isBefore(OffsetDateTime.now().minusSeconds(15)) == true
                        }
                        .map { it.lobby }
                        .toList()
                        .forEach { lobbyCodeToClean ->
                            activeConnections.removeAll { it.lobby == lobbyCodeToClean }
                            emit(lobbyCodeToClean)
                        }
                }
            }
        }

}
