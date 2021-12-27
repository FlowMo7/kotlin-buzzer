package dev.moetz.buzzer.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class BuzzingSessionManager {

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
        println("onParticipantEntered($id, $nickname)")
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
        println("onParticipantLeft($id, $nickname)")
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

    suspend fun createNewLobby(): String {
        println("createNewLobby()")
        return getNewLobbyCode()
    }

    suspend fun addBuzz(id: String, participantName: String) {
        println("addBuzz($id, $participantName)")
        val flow = getOrAddBuzzingSessionStateFlow(id)
        flowMutationMutex.withLock {
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

    suspend fun clearBuzzes(id: String) {
        println("clearBuzzes($id)")
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

}