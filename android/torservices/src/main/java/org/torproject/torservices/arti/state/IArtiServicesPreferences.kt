package org.torproject.torservices.arti.state

interface IArtiServicesPreferences {
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
}