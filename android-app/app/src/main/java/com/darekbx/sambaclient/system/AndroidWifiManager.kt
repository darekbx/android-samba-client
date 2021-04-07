package com.darekbx.sambaclient.system

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

class AndroidWifiManager(context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Suppress("DEPRECATION")
    fun getConnectionInfo(): WifiInfo? = wifiManager.connectionInfo
}
