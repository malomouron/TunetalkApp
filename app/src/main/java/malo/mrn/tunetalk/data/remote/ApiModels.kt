package malo.mrn.tunetalk.data.remote

import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.domain.model.UserSession
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val displayName: String,
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val accessToken: String,
    val expiresAt: String,
    val email: String,
    val displayName: String
) {
    fun toDomain(): UserSession {
        val expirationEpochSeconds = try {
            OffsetDateTime.parse(expiresAt).toEpochSecond()
        } catch (exception: DateTimeParseException) {
            throw IllegalArgumentException("The API returned an invalid token expiration.", exception)
        }

        return UserSession(
            accessToken = accessToken,
            email = email,
            displayName = displayName,
            expiresAtEpochSeconds = expirationEpochSeconds
        )
    }
}

data class GenreDto(
    val id: String,
    val name: String
) {
    fun toDomain() = Genre(id, name)
}

data class RadioChannelDto(
    val id: String,
    val name: String,
    val genre: String,
    val description: String?,
    val streamUrl: String?,
    val artworkUrl: String?
) {
    fun toDomain() = RadioChannel(id, name, genre, description, streamUrl, artworkUrl)
}

data class ChatMessageDto(
    val id: String,
    val channelId: String,
    val displayName: String,
    val content: String,
    val createdAt: String
) {
    fun toDomain() = ChatMessage(id, channelId, displayName, content, createdAt)
}
