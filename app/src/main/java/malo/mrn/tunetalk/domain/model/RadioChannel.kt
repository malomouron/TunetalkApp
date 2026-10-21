package malo.mrn.tunetalk.domain.model

data class RadioChannel(
    val id: String,
    val name: String,
    val genre: String,
    val description: String?,
    val streamUrl: String?,
    val artworkUrl: String?
)
