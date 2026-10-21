package malo.mrn.tunetalk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import malo.mrn.tunetalk.data.remote.ApiNetworkPolicy
import malo.mrn.tunetalk.presentation.AppViewModel
import malo.mrn.tunetalk.presentation.LocalNetworkPermissionScreen
import malo.mrn.tunetalk.presentation.TunetalkApp
import malo.mrn.tunetalk.ui.theme.TunetalkTheme

class MainActivity : ComponentActivity() {
    private var requiresLocalNetworkPermission = false
    private var hasLocalNetworkPermission by mutableStateOf(false)
    private var localNetworkPermissionDeniedPermanently by mutableStateOf(false)
    private var requestedLocalNetworkPermission = false

    private val localNetworkPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasLocalNetworkPermission = granted
            localNetworkPermissionDeniedPermanently = !granted
                && requestedLocalNetworkPermission
                && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiresLocalNetworkPermission = Build.VERSION.SDK_INT >= 37
            && ApiNetworkPolicy.requiresLocalNetworkPermission(BuildConfig.API_BASE_URL)
        updateLocalNetworkPermissionState()
        enableEdgeToEdge()
        setContent {
            TunetalkTheme {
                if (!requiresLocalNetworkPermission || hasLocalNetworkPermission) {
                    val container = (application as TunetalkApplication).container
                    val viewModel: AppViewModel = viewModel(factory = container.viewModelFactory)
                    TunetalkApp(viewModel)
                } else {
                    LocalNetworkPermissionScreen(
                        showSettingsAction = localNetworkPermissionDeniedPermanently,
                        onRequestPermission = {
                            requestedLocalNetworkPermission = true
                            localNetworkPermissionLauncher.launch(
                                Manifest.permission.ACCESS_LOCAL_NETWORK
                            )
                        },
                        onOpenSettings = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateLocalNetworkPermissionState()
    }

    private fun updateLocalNetworkPermissionState() {
        if (!requiresLocalNetworkPermission) {
            hasLocalNetworkPermission = true
            return
        }

        hasLocalNetworkPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocalNetworkPermission) {
            localNetworkPermissionDeniedPermanently = false
        }
    }
}