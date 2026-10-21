package malo.mrn.tunetalk.domain.repository

import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel

interface ChannelRepository {
    suspend fun getGenres(): List<Genre>

    suspend fun getChannels(genreId: String): List<RadioChannel>

    suspend fun getRecentMessages(channelId: String, accessToken: String): List<ChatMessage>
}
