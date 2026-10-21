package malo.mrn.tunetalk.data.repository

import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import malo.mrn.tunetalk.data.remote.TunetalkApi
import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.repository.ChatRepository
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder

class ChatConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)

class SignalRChatRepository(
    private val apiBaseUrl: String,
    private val api: TunetalkApi
) : ChatRepository {
    private val mutex = Mutex()
    private var connection: HubConnection? = null
    private var activeChannelId: String? = null
    private val mutableIncomingMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    private val mutableListenerCounts = MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 8)
    private val mutableConnectionErrors = MutableSharedFlow<String>(extraBufferCapacity = 4)

    override val incomingMessages: Flow<ChatMessage> = mutableIncomingMessages.asSharedFlow()
    override val listenerCounts: Flow<Pair<String, Int>> = mutableListenerCounts.asSharedFlow()
    override val connectionErrors: Flow<String> = mutableConnectionErrors.asSharedFlow()

    override suspend fun joinChannel(channelId: String, accessToken: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                closeCurrentConnection()

                val hub = HubConnectionBuilder
                    .create("${apiBaseUrl.trimEnd('/')}/hubs/chat")
                    .withAccessTokenProvider(Single.defer { Single.just(accessToken) })
                    .build()
                registerHandlers(hub)
                try {
                    hub.start().blockingAwait()
                    hub.invoke(Void::class.java, "JoinChannel", channelId).blockingGet()
                    connection = hub
                    activeChannelId = channelId
                } catch (exception: CancellationException) {
                    hub.stop().blockingAwait()
                    throw exception
                } catch (exception: RuntimeException) {
                    hub.stop().blockingAwait()
                    throw ChatConnectionException("Impossible de rejoindre le salon.", exception)
                }
            }
        }
    }

    override suspend fun sendMessage(content: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val hub = connection ?: throw ChatConnectionException("Le chat n’est pas connecté.")
                val channelId = activeChannelId
                    ?: throw ChatConnectionException("Aucun salon n’est sélectionné.")
                try {
                    hub.invoke(Void::class.java, "SendMessage", channelId, content).blockingGet()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: RuntimeException) {
                    throw ChatConnectionException("Le message n’a pas pu être envoyé.", exception)
                }
            }
        }
    }

    override suspend fun leaveChannel() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                closeCurrentConnection()
            }
        }
    }

    private fun registerHandlers(hub: HubConnection) {
        hub.on(
            "ReceiveMessage",
            { id: String, channelId: String, displayName: String, content: String, createdAt: String ->
                mutableIncomingMessages.tryEmit(
                    ChatMessage(id, channelId, displayName, content, createdAt)
                )
            },
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java,
            String::class.java
        )
        hub.on(
            "PresenceUpdated",
            { channelId: String, count: Int -> mutableListenerCounts.tryEmit(channelId to count) },
            String::class.java,
            Int::class.javaObjectType
        )
        hub.onClosed { exception ->
            if (exception != null) {
                mutableConnectionErrors.tryEmit(
                    exception.message ?: "La connexion au chat a été interrompue."
                )
            }
        }
    }

    private fun closeCurrentConnection() {
        val current = connection
        val channelId = activeChannelId
        connection = null
        activeChannelId = null

        if (current != null) {
            try {
                if (channelId != null) {
                    current.invoke(Void::class.java, "LeaveChannel", channelId).blockingGet()
                }
            } catch (exception: CancellationException) {
                current.stop().blockingAwait()
                throw exception
            } catch (exception: RuntimeException) {
                current.stop().blockingAwait()
                throw ChatConnectionException("Impossible de quitter le chat proprement.", exception)
            }

            current.stop().blockingAwait()
        }
    }
}
