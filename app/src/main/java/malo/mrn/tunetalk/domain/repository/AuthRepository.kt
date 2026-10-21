package malo.mrn.tunetalk.domain.repository

import malo.mrn.tunetalk.domain.model.UserSession

interface AuthRepository {
    suspend fun register(displayName: String, email: String, password: String): UserSession

    suspend fun login(email: String, password: String): UserSession

    suspend fun restoreSession(): UserSession?

    suspend fun logout()
}
