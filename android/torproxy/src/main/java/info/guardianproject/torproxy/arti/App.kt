package info.guardianproject.torproxy.arti

import android.app.Application
import android.util.Log
import info.guardianproject.artiservice.ArtiService

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("###", "App.onCreate()")

        // TODO: start activity or something to launch foreground service on boot?
    }
}