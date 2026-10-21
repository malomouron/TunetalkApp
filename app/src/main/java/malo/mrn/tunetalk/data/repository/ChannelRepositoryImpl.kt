package malo.mrn.tunetalk.data.repository

import malo.mrn.tunetalk.data.remote.TunetalkApi
import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.domain.repository.ChannelRepository

class ChannelRepositoryImpl(private val api: TunetalkApi) : ChannelRepository {
    override suspend fun getGenres(): List<Genre> =
        api.getGenres().map { it.toDomain() }

    override suspend fun getChannels(genreId: String): List<RadioChannel> =
        api.getChannels(genreId).map { it.toDomain() }

    override suspend fun getRecentMessages(channelId: String, accessToken: String): List<ChatMessage> =
        api.getRecentMessages(channelId, "Bearer $accessToken").map { it.toDomain() }
}
