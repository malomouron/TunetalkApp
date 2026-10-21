package malo.mrn.tunetalk.presentation.room

import android.content.ComponentName
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import malo.mrn.tunetalk.R
import malo.mrn.tunetalk.domain.model.ChatMessage
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.playback.RadioPlaybackService
import malo.mrn.tunetalk.presentation.AppUiState
import malo.mrn.tunetalk.ui.theme.TuneBackground
import malo.mrn.tunetalk.ui.theme.TuneCoral
import malo.mrn.tunetalk.ui.theme.TuneMint
import malo.mrn.tunetalk.ui.theme.TunePurple
import malo.mrn.tunetalk.ui.theme.TuneSurface
import malo.mrn.tunetalk.ui.theme.TuneSurfaceVariant
import malo.mrn.tunetalk.ui.theme.TuneText
import java.util.concurrent.ExecutionException

@Composable
fun RoomScreen(
    state: AppUiState,
    channel: RadioChannel,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var draft by rememberSaveable(channel.id) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuneBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = TuneText
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, color = TuneText, style = MaterialTheme.typography.titleMedium)
                Text(channel.genre, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.room_listeners, state.listenerCount),
                color = TuneMint,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(10.dp))
        RadioPlayerCard(channel)

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.room_chat_title),
            color = TuneText,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        state.error?.let { error ->
            Text(
                text = error,
                color = TuneCoral,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.isConnectingChat) {
                item {
                    Text(
                        text = stringResource(R.string.room_connecting),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (state.messages.isEmpty() && !state.isConnectingChat) {
                item {
                    Text(
                        text = stringResource(R.string.room_no_messages),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            items(state.messages, key = ChatMessage::id) { message ->
                MessageBubble(
                    message = message,
                    isOwnMessage = message.displayName == state.session?.displayName
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(500) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.room_chat_placeholder)) },
                enabled = state.isChatConnected,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
            )
            IconButton(
                onClick = {
                    onSendMessage(draft)
                    draft = ""
                },
                enabled = state.isChatConnected && draft.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = null,
                    tint = if (state.isChatConnected && draft.isNotBlank()) TuneMint
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RadioPlayerCard(channel: RadioChannel) {
    val context = LocalContext.current.applicationContext
    val genericPlayerError = stringResource(R.string.room_player_error)
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    val sessionToken = remember(context) {
        SessionToken(context, ComponentName(context, RadioPlaybackService::class.java))
    }

    DisposableEffect(sessionToken) {
        var disposed = false
        val future: ListenableFuture<MediaController> =
            MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                if (!disposed) {
                    try {
                        controller = future.get()
                    } catch (exception: ExecutionException) {
                        playbackError = exception.cause?.message
                            ?: "Le service de lecture n’a pas pu démarrer."
                    } catch (exception: InterruptedException) {
                        Thread.currentThread().interrupt()
                        playbackError = "La connexion au lecteur a été interrompue."
                    }
                }
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            disposed = true
            controller = null
            MediaController.releaseFuture(future)
        }
    }

    DisposableEffect(controller) {
        val player = controller
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.message ?: genericPlayerError
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    LaunchedEffect(controller, channel.id, channel.streamUrl) {
        val player = controller ?: return@LaunchedEffect
        val streamUrl = channel.streamUrl
        playbackError = null
        if (streamUrl.isNullOrBlank()) {
            player.stop()
            player.clearMediaItems()
            playbackError = "Aucun flux HTTPS disponible pour cette station."
        } else {
            val mediaItem = MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(channel.name)
                        .setArtist(channel.genre)
                        .build()
                )
                .build()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    Surface(
        color = TuneSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(TuneSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering || (controller == null && channel.streamUrl != null)) {
                    CircularProgressIndicator(
                        color = TuneMint,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("TT", color = TuneMint, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = TuneText,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = channel.description ?: channel.genre,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                if (playbackError != null) {
                    Text(
                        text = playbackError.orEmpty(),
                        color = TuneCoral,
                        style = MaterialTheme.typography.labelSmall
                    )
                } else if (controller == null) {
                    Text(
                        text = stringResource(R.string.room_player_connecting),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isPlaying) controller?.pause() else controller?.play()
                },
                enabled = controller != null && !channel.streamUrl.isNullOrBlank()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = TuneMint,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isOwnMessage) TunePurple else TuneSurfaceVariant,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isOwnMessage) 18.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 18.dp
            ),
            modifier = Modifier.fillMaxWidth(0.84f)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.displayName,
                        color = if (isOwnMessage) TuneBackground else TuneMint,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = message.createdAt.substringAfter('T').take(5),
                        color = if (isOwnMessage) TuneBackground.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.content,
                    color = if (isOwnMessage) TuneBackground else TuneText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
