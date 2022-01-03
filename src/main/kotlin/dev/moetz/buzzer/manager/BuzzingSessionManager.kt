package dev.moetz.buzzer.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class BuzzingSessionManager(
    private val buzzLogging: BuzzLogging
) {

    data class BuzzingSessionData(
        val id: String,
        val participantsState: List<ParticipantState>
    ) {
        data class ParticipantState(
            val index: Int,
            val name: String,
            val buzzed: Boolean
        )
    }

    private val buzzingSateMap: MutableMap<String, MutableStateFlow<BuzzingSessionData>> = mutableMapOf()

    private val buzzingStateAccessMutex = Mutex()

    private val flowMutationMutex = Mutex()


    private suspend fun getOrAddBuzzingSessionStateFlow(id: String): MutableStateFlow<BuzzingSessionData> {
        return buzzingStateAccessMutex.withLock {
            val data = buzzingSateMap[id]
            if (data == null) {
                MutableStateFlow(BuzzingSessionData(id = id, participantsState = emptyList()))
                    .also { buzzingSateMap[id] = it }
            } else {
                data
            }
        }
    }

    private suspend fun getNewLobbyCode(): String {
        return buzzingStateAccessMutex.withLock {
            var id: String
            do {
                id = UUID.randomUUID().toString().substringBefore("-").lowercase(Locale.getDefault())
            } while (buzzingSateMap.containsKey(id))

            MutableStateFlow(BuzzingSessionData(id = id, participantsState = emptyList()))
                .also { buzzingSateMap[id] = it }

            id
        }
    }

    suspend fun getBuzzerFlow(id: String): Flow<BuzzingSessionData> {
        return getOrAddBuzzingSessionStateFlow(id).asStateFlow()
    }

    suspend fun onParticipantEntered(id: String, nickname: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Participant, "$nickname joined")
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
            flow.value = flow.value.let { currentState ->
                currentState.copy(
                    participantsState = currentState.participantsState
                        .toMutableList()
                        .let { list ->
                            if (list.any { it.name == nickname }) {
                                //already in list
                                list
                            } else {
                                val indexToInsertAt = list.lastIndex + 1
                                list.add(
                                    indexToInsertAt,
                                    BuzzingSessionData.ParticipantState(indexToInsertAt, nickname, false)
                                )
                                list
                            }
                        }
                )
            }
        }
    }

    suspend fun onParticipantLeft(id: String, nickname: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Participant, "$nickname left")
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
            flow.value = flow.value.let { currentState ->
                currentState.copy(
                    participantsState = currentState.participantsState
                        .filterNot { it.name == nickname }
                )
            }
        }
    }

    suspend fun onHostEntered(id: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Host, "joined")
        //TODO
    }

    suspend fun onHostLeft(id: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Host, "left")
        //TODO
    }

    suspend fun createNewLobby(): String {
        return getNewLobbyCode().also { lobbyId ->
            buzzLogging.log(lobby = lobbyId, role = BuzzLogging.Role.Host, "created")
        }
    }

    suspend fun addBuzz(id: String, participantName: String) {
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
            val alreadyBuzzed = flow.value.participantsState.any { it.name == participantName && it.buzzed == true }
            if (alreadyBuzzed) {
                //Nothing to do here
            } else {
                buzzLogging.log(lobby = id, role = BuzzLogging.Role.Participant, "$participantName buzzed")
                flow.value = flow.value.let { currentState ->
                    currentState.copy(
                        participantsState = currentState.participantsState
                            .toMutableList()
                            .let { list ->
                                if (list.any { it.name == participantName }) {
                                    val indexOfLastBuzzed = list.indexOfLast { it.buzzed == true }
                                    list
                                        .mapIndexed { index, participantState ->
                                            if (participantState.name == participantName) {
                                                participantState.copy(
                                                    buzzed = true,
                                                    index = indexOfLastBuzzed + 1
                                                )
                                            } else {
                                                if (index > indexOfLastBuzzed) {
                                                    participantState.copy(index = participantState.index + 1)
                                                } else {
                                                    participantState
                                                }
                                            }
                                        }
                                        .sortedBy { it.index }
                                } else {
                                    val indexToInsertAt = list
                                        .indexOfLast { it.buzzed == true }
                                        .let {
                                            if (it == -1) {
                                                0
                                            } else {
                                                it + 1
                                            }
                                        }
                                    list.add(
                                        indexToInsertAt,
                                        BuzzingSessionData.ParticipantState(indexToInsertAt, participantName, true)
                                    )
                                    list
                                        .mapIndexed { index, participantState ->
                                            if (participantState.buzzed.not()) {
                                                participantState.copy(index = participantState.index + 1)
                                            } else {
                                                participantState
                                            }
                                        }
                                        .sortedBy { it.index }
                                }
                            }
                    )
                }
            }
        }
    }

    suspend fun clearBuzzes(id: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Host, "buzzes cleared")
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
            flow.value = flow.value.let { currentState ->
                currentState.copy(
                    participantsState = currentState.participantsState
                        .toMutableList()
                        .map { it.copy(buzzed = false) }
                )
            }
        }
    }

    suspend fun isValidLobbyCode(code: String): Boolean {
        val regex = "^[a-zA-Z0-9-_]*\$".toRegex()
        return regex.matches(code)
    }

    suspend fun isValidNickname(nickname: String): Boolean {
        val regex = "^[a-zA-Z0-9-_ ]*\$".toRegex()
        return regex.matches(nickname)
    }

}