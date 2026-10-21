package malo.mrn.tunetalk.domain.usecase

import malo.mrn.tunetalk.domain.repository.ChannelRepository

class LoadGenresUseCase(private val repository: ChannelRepository) {
    suspend operator fun invoke() = repository.getGenres()
}

class LoadChannelsUseCase(private val repository: ChannelRepository) {
    suspend operator fun invoke(genreId: String) = repository.getChannels(genreId)
}

class LoadChatHistoryUseCase(private val repository: ChannelRepository) {
    suspend operator fun invoke(channelId: String, accessToken: String) =
        repository.getRecentMessages(channelId, accessToken)
}
