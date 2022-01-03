package dev.moetz.buzzer.manager

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
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

    @Test
    fun testIsValidLobbyCodeLogic() {
        runBlocking {
            buzzingSessionManager.isValidLobbyCode("asdfAASD123123-_").shouldBeTrue()
            buzzingSessionManager.isValidLobbyCode("asdg32649846c9").shouldBeTrue()
            buzzingSessionManager.isValidLobbyCode("KJAGD8237648-_").shouldBeTrue()
            buzzingSessionManager.isValidLobbyCode("invalid space").shouldBeFalse()
            buzzingSessionManager.isValidLobbyCode("<script>").shouldBeFalse()
            buzzingSessionManager.isValidLobbyCode("\"").shouldBeFalse()
        }
    }

    @Test
    fun testIsValidNickname() {
        runBlocking {
            buzzingSessionManager.isValidNickname("asdfAASD123123-_ ").shouldBeTrue()
            buzzingSessionManager.isValidNickname("asdg32649846c9").shouldBeTrue()
            buzzingSessionManager.isValidNickname("KJAGD8237648-_").shouldBeTrue()
            buzzingSessionManager.isValidNickname("valid space").shouldBeTrue()
            buzzingSessionManager.isValidNickname("<script>").shouldBeFalse()
            buzzingSessionManager.isValidNickname("\"").shouldBeFalse()
        }
    }

}