package info.guardianproject.torproxy.arti.state

interface IArtiServicesPreferences {
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
}