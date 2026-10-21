package malo.mrn.tunetalk

import android.app.Application
import malo.mrn.tunetalk.di.AppContainer

class TunetalkApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
