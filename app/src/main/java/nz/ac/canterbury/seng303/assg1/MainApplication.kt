package nz.ac.canterbury.seng303.assg1

import android.app.Application
import android.util.Log
import kotlinx.coroutines.FlowPreview
import nz.ac.canterbury.seng303.assg1.datastore.dataAccessModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(this@MainApplication)
                modules(dataAccessModule)
            }
        } catch (e: Exception) {
            Log.e("MainApplication", "Error initializing Koin", e)
        }
    }
}