package malo.mrn.tunetalk.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import malo.mrn.tunetalk.presentation.auth.AuthScreen
import malo.mrn.tunetalk.presentation.home.HomeScreen
import malo.mrn.tunetalk.presentation.room.RoomScreen
import malo.mrn.tunetalk.ui.theme.TuneBackground
import malo.mrn.tunetalk.ui.theme.TunePurple

@Composable
fun TunetalkApp(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.selectedChannel != null) {
        viewModel.leaveChannel()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TuneBackground)
            .safeDrawingPadding()
    ) {
        when {
            state.isRestoringSession -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = TunePurple
            )

            state.session == null -> AuthScreen(
                state = state,
                onSubmit = viewModel::authenticate,
                onToggleMode = viewModel::toggleAuthMode
            )

            state.selectedChannel != null -> {
                val channel = state.selectedChannel
                if (channel != null) {
                    RoomScreen(
                        state = state,
                        channel = channel,
                        onBack = viewModel::leaveChannel,
                        onSendMessage = viewModel::sendMessage
                    )
                }
            }

            else -> HomeScreen(
                state = state,
                onSelectGenre = viewModel::selectGenre,
                onJoinChannel = viewModel::joinChannel,
                onRefreshCatalog = viewModel::retryCatalog,
                onLogout = viewModel::logout
            )
        }
    }
}
