package malo.mrn.tunetalk.domain.model

data class UserSession(
    val accessToken: String,
    val email: String,
    val displayName: String,
    val expiresAtEpochSeconds: Long
)
