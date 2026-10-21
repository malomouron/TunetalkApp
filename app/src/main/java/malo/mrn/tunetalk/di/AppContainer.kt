package malo.mrn.tunetalk.di

import android.content.Context
import malo.mrn.tunetalk.BuildConfig
import malo.mrn.tunetalk.data.local.SessionStore
import malo.mrn.tunetalk.data.remote.TunetalkApi
import malo.mrn.tunetalk.data.repository.AuthRepositoryImpl
import malo.mrn.tunetalk.data.repository.ChannelRepositoryImpl
import malo.mrn.tunetalk.data.repository.SignalRChatRepository
import malo.mrn.tunetalk.domain.usecase.LoadChatHistoryUseCase
import malo.mrn.tunetalk.domain.usecase.LoadChannelsUseCase
import malo.mrn.tunetalk.domain.usecase.LoadGenresUseCase
import malo.mrn.tunetalk.domain.usecase.LoginUseCase
import malo.mrn.tunetalk.domain.usecase.RegisterUseCase
import malo.mrn.tunetalk.presentation.AppViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val apiBaseUrl = BuildConfig.API_BASE_URL.ensureTrailingSlash()
    private val api = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TunetalkApi::class.java)

    private val sessionStore = SessionStore(context)
    private val authRepository = AuthRepositoryImpl(api, sessionStore)
    private val channelRepository = ChannelRepositoryImpl(api)
    private val chatRepository = SignalRChatRepository(apiBaseUrl, api)

    val viewModelFactory = TunetalkViewModelFactory(
        appViewModel = {
            AppViewModel(
                authRepository = authRepository,
                loadGenres = LoadGenresUseCase(channelRepository),
                loadChannels = LoadChannelsUseCase(channelRepository),
                loadChatHistory = LoadChatHistoryUseCase(channelRepository),
                login = LoginUseCase(authRepository),
                register = RegisterUseCase(authRepository),
                chatRepository = chatRepository
            )
        }
    )

    private fun String.ensureTrailingSlash(): String =
        if (endsWith('/')) this else "$this/"
}
