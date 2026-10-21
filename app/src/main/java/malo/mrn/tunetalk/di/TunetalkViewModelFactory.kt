package malo.mrn.tunetalk.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import malo.mrn.tunetalk.presentation.AppViewModel

class TunetalkViewModelFactory(
    private val appViewModel: () -> AppViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(AppViewModel::class.java)) {
            throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return appViewModel() as T
    }
}
