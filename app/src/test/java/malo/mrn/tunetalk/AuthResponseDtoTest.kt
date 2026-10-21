package malo.mrn.tunetalk

import malo.mrn.tunetalk.data.remote.AuthResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class AuthResponseDtoTest {
    @Test
    fun convertsDotNetDateTimeOffsetExpirationWithSevenFractionalDigits() {
        val response = AuthResponseDto(
            accessToken = "header.payload.signature",
            expiresAt = "2026-10-21T12:44:45.9322641+00:00",
            email = "listener@example.com",
            displayName = "Listener"
        )

        val session = response.toDomain()

        assertEquals(
            Instant.parse("2026-10-21T12:44:45Z").epochSecond,
            session.expiresAtEpochSeconds
        )
    }

    @Test
    fun rejectsInvalidExpirationWithoutThrowingADateTimeParseException() {
        val response = AuthResponseDto(
            accessToken = "header.payload.signature",
            expiresAt = "not-an-iso-offset-date",
            email = "listener@example.com",
            displayName = "Listener"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            response.toDomain()
        }

        assertEquals("The API returned an invalid token expiration.", exception.message)
    }
}
