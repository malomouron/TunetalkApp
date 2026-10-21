package malo.mrn.tunetalk.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import malo.mrn.tunetalk.R
import malo.mrn.tunetalk.ui.theme.TuneBackground
import malo.mrn.tunetalk.ui.theme.TuneMint
import malo.mrn.tunetalk.ui.theme.TunePurple
import malo.mrn.tunetalk.ui.theme.TuneSurface
import malo.mrn.tunetalk.ui.theme.TuneText

@Composable
fun LocalNetworkPermissionScreen(
    showSettingsAction: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuneBackground)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Surface(shape = CircleShape, color = TuneSurface) {
            Text(
                text = "TUNE / TALK",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                color = TuneMint,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.local_network_permission_title),
            color = TuneText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.local_network_permission_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.local_network_permission_button))
        }
        if (showSettingsAction) {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = stringResource(R.string.local_network_permission_settings),
                    color = TunePurple
                )
            }
        }
    }
}
