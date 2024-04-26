package info.guardianproject.torproxy.arti.state

import android.content.Context

class ArtiServicesPreferences(context: Context) : IArtiServicesPreferences {

    companion object {
        private const val ENABLED_KEY = "enabled";
        private const val ENABLED_DEFAULT = true;

        @Volatile
        private var instance: ArtiServicesPreferences? = null

        fun with(context: Context) = instance ?: synchronized(this) {
            instance ?: ArtiServicesPreferences(context).also { instance = it }
        }
    }

    private val prefs =
        context.getSharedPreferences("torServicePreferences.properties", Context.MODE_PRIVATE)

    override fun isEnabled(): Boolean {
        return prefs.getBoolean(ENABLED_KEY, ENABLED_DEFAULT)
    }

    override fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(ENABLED_KEY, enabled).apply()
    }
}