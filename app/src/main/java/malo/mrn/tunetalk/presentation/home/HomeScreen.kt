package malo.mrn.tunetalk.presentation.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import malo.mrn.tunetalk.R
import malo.mrn.tunetalk.domain.model.Genre
import malo.mrn.tunetalk.domain.model.RadioChannel
import malo.mrn.tunetalk.presentation.AppUiState
import malo.mrn.tunetalk.ui.theme.TuneBackground
import malo.mrn.tunetalk.ui.theme.TuneCoral
import malo.mrn.tunetalk.ui.theme.TuneMint
import malo.mrn.tunetalk.ui.theme.TunePurple
import malo.mrn.tunetalk.ui.theme.TuneSurface
import malo.mrn.tunetalk.ui.theme.TuneSurfaceVariant
import malo.mrn.tunetalk.ui.theme.TuneText

@Composable
fun HomeScreen(
    state: AppUiState,
    onSelectGenre: (Genre) -> Unit,
    onJoinChannel: (RadioChannel) -> Unit,
    onRefreshCatalog: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuneBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TUNE / TALK",
                color = TuneMint,
                style = MaterialTheme.typography.labelSmall
            )
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.home_logout), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.home_eyebrow),
            color = TunePurple,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_title),
            color = TuneText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.home_genres),
            color = TuneText,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.genres, key = Genre::id) { genre ->
                val selected = genre.id == state.selectedGenre?.id
                TextButton(
                    onClick = { onSelectGenre(genre) },
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selected) TunePurple else TuneSurface,
                        contentColor = if (selected) TuneBackground else TuneText
                    )
                ) {
                    Text(genre.name, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_channels),
            color = TuneText,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(10.dp))

        if (state.error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.error,
                    color = TuneCoral,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRefreshCatalog) {
                    Text(stringResource(R.string.home_retry), color = TuneMint)
                }
            }
        }

        when {
            state.isLoadingChannels -> Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TunePurple)
            }

            state.channels.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.channels, key = RadioChannel::id) { channel ->
                    ChannelCard(channel = channel, onListen = { onJoinChannel(channel) })
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(channel: RadioChannel, onListen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = TuneSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(TuneSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.name.firstOrNull()?.uppercase() ?: "T",
                    color = TuneMint,
                    style = MaterialTheme.typography.titleLarge
                )
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
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.channel_now_live),
                    color = TuneMint,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick = onListen,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TunePurple),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 13.dp,
                    vertical = 10.dp
                )
            ) {
                Text(stringResource(R.string.channel_listen), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
