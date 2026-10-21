package malo.mrn.tunetalk.data.remote

import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.domain.model.UserSession
import java.time.Instant

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
    fun toDomain(): UserSession = UserSession(
        accessToken = accessToken,
        email = email,
        displayName = displayName,
        expiresAtEpochSeconds = Instant.parse(expiresAt).epochSecond
    )
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
