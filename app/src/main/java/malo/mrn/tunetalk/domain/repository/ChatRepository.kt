package malo.mrn.tunetalk.domain.repository

import kotlinx.coroutines.flow.Flow
import malo.mrn.tunetalk.domain.model.ChatMessage

interface ChatRepository {
    val incomingMessages: Flow<ChatMessage>

    val listenerCounts: Flow<Pair<String, Int>>

    val connectionErrors: Flow<String>

    suspend fun joinChannel(channelId: String, accessToken: String)

    suspend fun sendMessage(content: String)

    suspend fun leaveChannel()
}
