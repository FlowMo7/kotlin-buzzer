package dev.moetz.buzzer.manager

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime

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
    fun test1() {
        runBlocking {
            assertEquals(
                BuzzingSessionManager.BuzzingSessionData(
                    id = "lobby1",
                    participantsState = emptyList()
                ),
                buzzingSessionManager.getBuzzerFlow("lobby1").first()
            )

            buzzingSessionManager.addBuzz("lobby1", "Participant1")

            verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }


            val buzzData = buzzingSessionManager.getBuzzerFlow("lobby1").first()
            buzzData.id shouldBeEqualTo "lobby1"
            buzzData.participantsState.count() shouldBeEqualTo 1
            buzzData.participantsState.first().index shouldBeEqualTo 0
            buzzData.participantsState.first().name shouldBeEqualTo "Participant1"
            buzzData.participantsState.first().buzzed shouldBeEqualTo true
            buzzData.participantsState.first().buzzedAt.shouldNotBeNull()
            buzzData.participantsState.first().buzzedAt!!.shouldBeInRange(
                OffsetDateTime.now().minusSeconds(3),
                OffsetDateTime.now()
            )
        }
    }

    @Test
    fun test2() {
        runBlocking {
            assertEquals(
                BuzzingSessionManager.BuzzingSessionData(
                    id = "lobby1",
                    participantsState = emptyList()
                ),
                buzzingSessionManager.getBuzzerFlow("lobby1").first()
            )

            buzzingSessionManager.addBuzz("lobby1", "Participant1")

            verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }

            buzzingSessionManager.getBuzzerFlow("lobby1").first().apply {
                id shouldBeEqualTo "lobby1"
                participantsState.count() shouldBeEqualTo 1
                participantsState.first().index shouldBeEqualTo 0
                participantsState.first().name shouldBeEqualTo "Participant1"
                participantsState.first().buzzed shouldBeEqualTo true
                participantsState.first().buzzedAt.shouldNotBeNull()
                participantsState.first().buzzedAt!!.shouldBeInRange(
                    OffsetDateTime.now().minusSeconds(3),
                    OffsetDateTime.now()
                )
            }


            buzzingSessionManager.clearBuzzes("lobby1")

            verify { buzzLogging.log("lobby1", BuzzLogging.Role.Host, "buzzes cleared") }

            buzzingSessionManager.getBuzzerFlow("lobby1").first().apply {
                id shouldBeEqualTo "lobby1"
                participantsState.count() shouldBeEqualTo 1
                participantsState.first().index shouldBeEqualTo 0
                participantsState.first().name shouldBeEqualTo "Participant1"
                participantsState.first().buzzed shouldBeEqualTo false
                participantsState.first().buzzedAt.shouldBeNull()
            }


            buzzingSessionManager.addBuzz("lobby1", "Participant1")

            verify { buzzLogging.log("lobby1", BuzzLogging.Role.Participant, "Participant1 buzzed") }

            buzzingSessionManager.getBuzzerFlow("lobby1").first().apply {
                id shouldBeEqualTo "lobby1"
                participantsState.count() shouldBeEqualTo 1
                participantsState.first().index shouldBeEqualTo 0
                participantsState.first().name shouldBeEqualTo "Participant1"
                participantsState.first().buzzed shouldBeEqualTo true
                participantsState.first().buzzedAt.shouldNotBeNull()
                participantsState.first().buzzedAt!!.shouldBeInRange(
                    OffsetDateTime.now().minusSeconds(3),
                    OffsetDateTime.now()
                )
            }
        }
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