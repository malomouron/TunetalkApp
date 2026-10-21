package malo.mrn.tunetalk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import malo.mrn.tunetalk.data.repository.ChatConnectionException
import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.domain.model.UserSession
import malo.mrn.tunetalk.domain.repository.AuthRepository
import malo.mrn.tunetalk.domain.repository.ChatRepository
import malo.mrn.tunetalk.domain.usecase.LoadChatHistoryUseCase
import malo.mrn.tunetalk.domain.usecase.LoadChannelsUseCase
import malo.mrn.tunetalk.domain.usecase.LoadGenresUseCase
import malo.mrn.tunetalk.domain.usecase.LoginUseCase
import malo.mrn.tunetalk.domain.usecase.RegisterUseCase
import malo.mrn.tunetalk.domain.validation.AuthFormValidator
import retrofit2.HttpException
import java.io.IOException

data class AppUiState(
    val session: UserSession? = null,
    val genres: List<Genre> = emptyList(),
    val selectedGenre: Genre? = null,
    val channels: List<RadioChannel> = emptyList(),
    val selectedChannel: RadioChannel? = null,
    val messages: List<ChatMessage> = emptyList(),
    val listenerCount: Int = 0,
    val isRegistering: Boolean = false,
    val isRestoringSession: Boolean = true,
    val isSubmitting: Boolean = false,
    val isLoadingChannels: Boolean = false,
    val isConnectingChat: Boolean = false,
    val isChatConnected: Boolean = false,
    val error: String? = null
)

class AppViewModel(
    private val authRepository: AuthRepository,
    private val loadGenres: LoadGenresUseCase,
    private val loadChannels: LoadChannelsUseCase,
    private val loadChatHistory: LoadChatHistoryUseCase,
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AppUiState())
    val uiState = mutableUiState.asStateFlow()

    init {
        observeChatEvents()
        restoreSession()
    }

    fun toggleAuthMode() {
        mutableUiState.update { it.copy(isRegistering = !it.isRegistering, error = null) }
    }

    fun authenticate(displayName: String, email: String, password: String) {
        val registering = mutableUiState.value.isRegistering
        val validationError = AuthFormValidator.validate(displayName, email, password, registering)
        if (validationError != null) {
            mutableUiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val session = if (registering) {
                    register(displayName, email, password)
                } else {
                    login(email, password)
                }
                mutableUiState.update { it.copy(session = session, isSubmitting = false) }
                refreshCatalog()
            } catch (exception: HttpException) {
                mutableUiState.update { it.copy(isSubmitting = false, error = exception.readableMessage()) }
            } catch (exception: IOException) {
                mutableUiState.update {
                    it.copy(isSubmitting = false, error = "Connexion impossible. Vérifiez votre réseau et l’API.")
                }
            } catch (exception: IllegalArgumentException) {
                mutableUiState.update { it.copy(isSubmitting = false, error = exception.message) }
            }
        }
    }

    fun selectGenre(genre: Genre) {
        mutableUiState.update { it.copy(selectedGenre = genre, isLoadingChannels = true, error = null) }
        viewModelScope.launch {
            try {
                val channels = loadChannels(genre.id)
                mutableUiState.update { it.copy(channels = channels, isLoadingChannels = false) }
            } catch (exception: HttpException) {
                mutableUiState.update { it.copy(isLoadingChannels = false, error = exception.readableMessage()) }
            } catch (exception: IOException) {
                mutableUiState.update {
                    it.copy(isLoadingChannels = false, error = "Le catalogue radio est momentanément inaccessible.")
                }
            }
        }
    }

    fun retryCatalog() {
        viewModelScope.launch {
            refreshCatalog()
        }
    }

    fun joinChannel(channel: RadioChannel) {
        val session = mutableUiState.value.session ?: return
        mutableUiState.update {
            it.copy(
                selectedChannel = channel,
                messages = emptyList(),
                listenerCount = 0,
                isConnectingChat = true,
                isChatConnected = false,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                chatRepository.joinChannel(channel.id, session.accessToken)
                mutableUiState.update {
                    if (it.selectedChannel?.id == channel.id) {
                        it.copy(isConnectingChat = false, isChatConnected = true)
                    } else {
                        it
                    }
                }
            } catch (exception: ChatConnectionException) {
                mutableUiState.update {
                    it.copy(
                        isConnectingChat = false,
                        isChatConnected = false,
                        error = exception.message
                    )
                }
                return@launch
            }

            try {
                val history = loadChatHistory(channel.id, session.accessToken)
                mutableUiState.update {
                    if (it.selectedChannel?.id != channel.id) {
                        it
                    } else {
                        it.copy(
                            messages = mergeMessages(it.messages, history),
                            error = null
                        )
                    }
                }
            } catch (exception: HttpException) {
                mutableUiState.update {
                    it.copy(error = exception.readableMessage())
                }
            } catch (exception: IOException) {
                mutableUiState.update {
                    it.copy(error = "Impossible de charger l’historique du chat.")
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val normalized = content.trim()
        if (normalized.isEmpty() || normalized.length > 500) {
            mutableUiState.update { it.copy(error = "Un message doit contenir entre 1 et 500 caractères.") }
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(normalized)
                mutableUiState.update { it.copy(error = null) }
            } catch (exception: ChatConnectionException) {
                mutableUiState.update { it.copy(error = exception.message) }
            }
        }
    }

    fun leaveChannel() {
        viewModelScope.launch {
            try {
                chatRepository.leaveChannel()
            } catch (exception: ChatConnectionException) {
                mutableUiState.update { it.copy(error = exception.message) }
            } finally {
                mutableUiState.update {
                    it.copy(
                        selectedChannel = null,
                        messages = emptyList(),
                        listenerCount = 0,
                        isConnectingChat = false,
                        isChatConnected = false
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                chatRepository.leaveChannel()
            } catch (exception: ChatConnectionException) {
                mutableUiState.update { it.copy(error = exception.message) }
            }

            try {
                authRepository.logout()
                mutableUiState.value = AppUiState(isRestoringSession = false)
            } catch (exception: IOException) {
                mutableUiState.update { it.copy(error = "La session n’a pas pu être supprimée.") }
            }
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                val session = authRepository.restoreSession()
                mutableUiState.update { it.copy(session = session, isRestoringSession = false) }
                if (session != null) {
                    refreshCatalog()
                }
            } catch (exception: IOException) {
                mutableUiState.update {
                    it.copy(
                        isRestoringSession = false,
                        error = "La session enregistrée n’a pas pu être lue."
                    )
                }
            }
        }
    }

    private suspend fun refreshCatalog() {
        mutableUiState.update { it.copy(isLoadingChannels = true, error = null) }
        try {
            val genres = loadGenres()
            val firstGenre = genres.firstOrNull()
            mutableUiState.update {
                it.copy(
                    genres = genres,
                    selectedGenre = firstGenre,
                    isLoadingChannels = false
                )
            }
            if (firstGenre != null) {
                val channels = loadChannels(firstGenre.id)
                mutableUiState.update { it.copy(channels = channels) }
            }
        } catch (exception: HttpException) {
            mutableUiState.update { it.copy(isLoadingChannels = false, error = exception.readableMessage()) }
        } catch (exception: IOException) {
            mutableUiState.update {
                it.copy(isLoadingChannels = false, error = "Le catalogue radio est momentanément inaccessible.")
            }
        }
    }

    private fun observeChatEvents() {
        viewModelScope.launch {
            chatRepository.incomingMessages.collect { message ->
                mutableUiState.update { state ->
                    if (state.selectedChannel?.id != message.channelId) {
                        state
                    } else {
                        state.copy(messages = mergeMessages(state.messages, listOf(message)))
                    }
                }
            }
        }
        viewModelScope.launch {
            chatRepository.listenerCounts.collect { (channelId, count) ->
                mutableUiState.update { state ->
                    if (state.selectedChannel?.id == channelId) state.copy(listenerCount = count) else state
                }
            }
        }
        viewModelScope.launch {
            chatRepository.connectionErrors.collect { message ->
                mutableUiState.update { it.copy(error = message, isChatConnected = false) }
            }
        }
    }

    private suspend fun HttpException.readableMessage(): String = withContext(Dispatchers.IO) {
        when (code()) {
            401 -> return@withContext "E-mail ou mot de passe incorrect."
            429 -> return@withContext "Trop de tentatives. Réessayez dans quelques secondes."
            503 -> return@withContext "Le catalogue radio est momentanément inaccessible."
        }

        val body = response()?.errorBody()?.string()
        if (body.isNullOrBlank()) {
            return@withContext "La requête a échoué (HTTP ${code()})."
        }

        try {
            val root = JsonParser.parseString(body)
            if (root.isJsonObject) {
                val problem = root.asJsonObject
                val detail = problem.get("detail")
                if (detail != null && !detail.isJsonNull && detail.asString.isNotBlank()) {
                    return@withContext detail.asString
                }

                val errors = problem.getAsJsonObject("errors")
                val firstError = errors?.entrySet()
                    ?.firstOrNull()
                    ?.value
                    ?.takeIf { it.isJsonArray && it.asJsonArray.size() > 0 }
                    ?.asJsonArray
                    ?.firstOrNull()
                if (firstError != null && !firstError.isJsonNull) {
                    return@withContext firstError.asString
                }
            }
        } catch (exception: JsonParseException) {
            return@withContext "La requête a échoué (HTTP ${code()})."
        }

        "La requête a échoué (HTTP ${code()})."
    }

    private fun mergeMessages(
        current: List<ChatMessage>,
        additions: List<ChatMessage>
    ): List<ChatMessage> =
        (current + additions)
            .distinctBy(ChatMessage::id)
            .sortedBy(ChatMessage::createdAt)
}
