package com.darekbx.sambaclient.util

import com.darekbx.sambaclient.system.AndroidWifiManager

class WifiUtils(private val androidWifiManager: AndroidWifiManager) {

    fun connectedSsid(): String? {
        return androidWifiManager.getConnectionInfo()?.ssid
    }
}
