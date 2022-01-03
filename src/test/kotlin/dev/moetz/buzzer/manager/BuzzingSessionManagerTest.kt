package dev.moetz.buzzer.manager

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BuzzingSessionManagerTest {

    private lateinit var buzzingSessionManager: BuzzingSessionManager
    private lateinit var buzzLogging: BuzzLogging

    @Before
    fun setUp() {
        buzzLogging = mockk(relaxed = true)

        buzzingSessionManager = BuzzingSessionManager(
            buzzLogging = buzzLogging
        )
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

        verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }

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

        verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }

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

        verify { buzzLogging.log("lobby1", BuzzLogging.Role.Host, "buzzes cleared") }

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

        verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }

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