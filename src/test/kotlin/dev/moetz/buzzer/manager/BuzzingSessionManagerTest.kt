package dev.moetz.buzzer.manager

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BuzzingSessionManagerTest {

    private lateinit var buzzingSessionManager: BuzzingSessionManager

    @Before
    fun setUp() {
        buzzingSessionManager = BuzzingSessionManager()
    }

    @Test
    fun test1() = runBlocking {
        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = emptyList()
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )

        buzzingSessionManager.addBuzz("lobby1", "Participant1")

        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = listOf(
                    BuzzingSessionManager.BuzzingSessionData.ParticipantState(
                        index = 0,
                        name = "Participant1",
                        buzzed = true
                    )
                )
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )
    }

    @Test
    fun test2() = runBlocking {
        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = emptyList()
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )

        buzzingSessionManager.addBuzz("lobby1", "Participant1")

        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = listOf(
                    BuzzingSessionManager.BuzzingSessionData.ParticipantState(
                        index = 0,
                        name = "Participant1",
                        buzzed = true
                    )
                )
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )

        buzzingSessionManager.clearBuzzes("lobby1")

        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = listOf(
                    BuzzingSessionManager.BuzzingSessionData.ParticipantState(
                        index = 0,
                        name = "Participant1",
                        buzzed = false
                    )
                )
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )

        buzzingSessionManager.addBuzz("lobby1", "Participant1")

        assertEquals(
            BuzzingSessionManager.BuzzingSessionData(
                id = "lobby1",
                participantsState = listOf(
                    BuzzingSessionManager.BuzzingSessionData.ParticipantState(
                        index = 0,
                        name = "Participant1",
                        buzzed = true
                    )
                )
            ),
            buzzingSessionManager.getBuzzerFlow("lobby1").first()
        )
    }

}