package malo.mrn.tunetalk.domain.model

data class ChatMessage(
    val id: String,
    val channelId: String,
    val displayName: String,
    val content: String,
    val createdAt: String
)
