package malo.mrn.tunetalk.domain.usecase

import malo.mrn.tunetalk.domain.model.UserSession
import malo.mrn.tunetalk.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): UserSession =
        repository.login(email, password)
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(displayName: String, email: String, password: String): UserSession =
        repository.register(displayName, email, password)
}
