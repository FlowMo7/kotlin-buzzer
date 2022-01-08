package dev.moetz.buzzer.manager

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.OffsetDateTime
import java.util.*

class BuzzingSessionManager(
    private val buzzLogging: BuzzLogging,
    connectionCountManager: ConnectionCountManager,
    private val lobbyCodeLength: Int
) {

    data class BuzzingSessionData(
        val id: String,
        val hostSecret: String?,
        val participantsState: List<ParticipantState>
    ) {
        data class ParticipantState(
            val index: Int,
            val name: String,
            val buzzed: Boolean,
            val buzzedAt: OffsetDateTime?
        )
    }

    init {
        connectionCountManager.cleanUpLobbyFlow
            .onEach { lobbyCodeToClean ->
                flowMutationMutex.withLock {
                    buzzLogging.log(lobbyCodeToClean, BuzzLogging.Role.System, "Clearing state")
                    buzzingSateMap.remove(lobbyCodeToClean)
                }
            }
            .launchIn(GlobalScope)
    }

    private val buzzingSateMap: MutableMap<String, MutableStateFlow<BuzzingSessionData>> = mutableMapOf()

    private val buzzingStateAccessMutex = Mutex()

    private val flowMutationMutex = Mutex()


    private fun generateRandomString(length: Int): String {
        var random = ""
        do {
            random += UUID.randomUUID().toString().replace("-", "").lowercase(Locale.getDefault())
        } while (random.length < length)

        return random.substring(0, length)
    }

    private suspend fun getOrAddBuzzingSessionStateFlow(id: String): MutableStateFlow<BuzzingSessionData> {
        return buzzingStateAccessMutex.withLock {
            val data = buzzingSateMap[id]
            if (data == null) {
                MutableStateFlow(
                    BuzzingSessionData(
                        id = id,
                        hostSecret = null,
                        participantsState = emptyList()
                    )
                )
                    .also { buzzingSateMap[id] = it }
            } else {
                data
            }
        }
    }

    private suspend fun getNewLobbyCode(): Pair<String, String> {
        return buzzingStateAccessMutex.withLock {
            var id: String
            do {
                id = generateRandomString(lobbyCodeLength)
            } while (buzzingSateMap.containsKey(id))

            val hostPassword = generateRandomString(6)

            MutableStateFlow(
                BuzzingSessionData(
                    id = id,
                    hostSecret = hostPassword,
                    participantsState = emptyList()
                )
            )
                .also { buzzingSateMap[id] = it }

            id to hostPassword
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
                                    BuzzingSessionData.ParticipantState(
                                        index = indexToInsertAt,
                                        name = nickname,
                                        buzzed = false,
                                        buzzedAt = null
                                    )
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

    fun onHostEntered(id: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Host, "joined")
        //TODO
    }

    fun onHostLeft(id: String) {
        buzzLogging.log(lobby = id, role = BuzzLogging.Role.Host, "left")
        //TODO
    }

    suspend fun createNewLobby(): Pair<String, String> {
        return getNewLobbyCode().also { (lobbyCode, _) ->
            buzzLogging.log(lobby = lobbyCode, role = BuzzLogging.Role.Host, "created")
        }
    }

    suspend fun addBuzz(id: String, participantName: String) {
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
            val alreadyBuzzed = flow.value.participantsState.any { it.name == participantName && it.buzzed == true }
            if (alreadyBuzzed) {
                //Nothing to do here
            } else {
                val dateTime = OffsetDateTime.now()
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
                                                    index = indexOfLastBuzzed + 1,
                                                    buzzedAt = dateTime
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
                                        BuzzingSessionData.ParticipantState(
                                            index = indexToInsertAt,
                                            name = participantName,
                                            buzzed = true,
                                            buzzedAt = dateTime
                                        )
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
                        .map { participantState ->
                            participantState.copy(
                                buzzed = false,
                                buzzedAt = null
                            )
                        }
                )
            }
        }
    }

    suspend fun verifyHostSecret(id: String, secret: String, applyIfNoSecretSetYet: Boolean = false): Boolean {
        return flowMutationMutex.withLock {
            val flow = getOrAddBuzzingSessionStateFlow(id)
            val currentSecret = flow.value.hostSecret
            if (currentSecret.isNullOrBlank()) {
                //No secret set on lobby -> auth succeeds
                if (applyIfNoSecretSetYet) {
                    flow.value = flow.value.copy(hostSecret = secret)
                }
                true
            } else {
                currentSecret == secret
            }
        }
    }

    fun isValidLobbyCode(code: String): Boolean {
        val regex = "^[a-zA-Z0-9-_]*\$".toRegex()
        return regex.matches(code)
    }

    fun isValidNickname(nickname: String): Boolean {
        val regex = "^[a-zA-Z0-9-_ ]*\$".toRegex()
        return regex.matches(nickname)
    }

}