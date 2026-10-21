package malo.mrn.tunetalk.data.repository

import malo.mrn.tunetalk.data.local.SessionStore
import malo.mrn.tunetalk.data.remote.LoginRequestDto
import malo.mrn.tunetalk.data.remote.RegisterRequestDto
import malo.mrn.tunetalk.data.remote.TunetalkApi
import malo.mrn.tunetalk.domain.model.UserSession
import malo.mrn.tunetalk.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: TunetalkApi,
    private val sessionStore: SessionStore
) : AuthRepository {
    override suspend fun register(displayName: String, email: String, password: String): UserSession {
        val session = api.register(RegisterRequestDto(displayName.trim(), email.trim(), password)).toDomain()
        sessionStore.write(session)
        return session
    }

    override suspend fun login(email: String, password: String): UserSession {
        val session = api.login(LoginRequestDto(email.trim(), password)).toDomain()
        sessionStore.write(session)
        return session
    }

    override suspend fun restoreSession(): UserSession? = sessionStore.read()

    override suspend fun logout() = sessionStore.clear()
}
