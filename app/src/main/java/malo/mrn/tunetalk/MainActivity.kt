package malo.mrn.tunetalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import malo.mrn.tunetalk.presentation.AppViewModel
import malo.mrn.tunetalk.presentation.TunetalkApp
import malo.mrn.tunetalk.ui.theme.TunetalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TunetalkTheme {
                val container = (application as TunetalkApplication).container
                val viewModel: AppViewModel = viewModel(factory = container.viewModelFactory)
                TunetalkApp(viewModel)
            }
        }
    }
}