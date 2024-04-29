package info.guardianproject.torproxy.arti

import android.app.Application
import android.util.Log
import info.guardianproject.artiservice.ArtiService.ArtiServiceConnection
import info.guardianproject.artiservice.ArtiService.ArtiServiceListener

class App : Application() {

    companion object {

        val artiServiceConnection = ArtiServiceConnection(object : ArtiServiceListener {
            override fun serviceConnected(serviceConnection: ArtiServiceConnection) {
                // start proxy when service was bound successfully//Log
                serviceConnection.startProxy()
            }

            override fun serviceDisconnected(serviceConnection: ArtiServiceConnection) {
            }

            override fun log(logLine: String) {
                // forward logs from arti
                Log.i("arti", "arti: $logLine")
            }

            override fun proxyStarted() {
            }

            override fun proxyStopped() {
            }
        })
    }

    override fun onCreate() {
        super.onCreate()

        // bind service on app start
        artiServiceConnection.bindArtiService(this)
    }
}