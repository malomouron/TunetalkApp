package malo.mrn.tunetalk.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import malo.mrn.tunetalk.R
import malo.mrn.tunetalk.presentation.AppUiState
import malo.mrn.tunetalk.ui.theme.TuneCoral
import malo.mrn.tunetalk.ui.theme.TuneMint
import malo.mrn.tunetalk.ui.theme.TunePurple
import malo.mrn.tunetalk.ui.theme.TuneSurface
import malo.mrn.tunetalk.ui.theme.TuneText

@Composable
fun AuthScreen(
    state: AppUiState,
    onSubmit: (String, String, String) -> Unit,
    onToggleMode: () -> Unit
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isRegistration = state.isRegistering

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(malo.mrn.tunetalk.ui.theme.TuneBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = TuneSurface,
            modifier = Modifier.padding(bottom = 22.dp)
        ) {
            Text(
                text = "TUNE / TALK",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                color = TuneMint,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = stringResource(if (isRegistration) R.string.auth_register_title else R.string.auth_login_title),
            color = TuneText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.tagline),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(30.dp))

        if (isRegistration) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.auth_display_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors()
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors()
        )

        state.error?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = TuneCoral,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onSubmit(displayName, email, password) },
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TunePurple)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    stringResource(if (isRegistration) R.string.auth_register_button else R.string.auth_login_button),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(if (isRegistration) R.string.auth_switch_to_login else R.string.auth_switch_to_register),
                color = TuneMint
            )
        }
    }
}
