package com.darekbx.sambaclient

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.remoteaccess.RemoteAccess
import com.darekbx.sambaclient.system.AndroidWifiManager
import com.darekbx.sambaclient.statistics.RemoteStatistics
import com.darekbx.sambaclient.samba.PathMovement
import com.darekbx.sambaclient.samba.SambaClientWrapper
import com.darekbx.sambaclient.viewmodel.StatisticsViewModel
import com.darekbx.sambaclient.viewmodel.SambaAccessViewModel
import com.darekbx.sambaclient.viewmodel.UriViewModel
import com.darekbx.sambaclient.util.UriUtils
import com.darekbx.sambaclient.util.WifiUtils
import com.darekbx.sambaclient.viewmodel.RemoteAccessViewModel
import com.google.gson.Gson
import com.hierynomus.smbj.SMBClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class SambaClientApplication : Application() {

    private val commonModule = module {
        single { SMBClient() }
        single { RemoteAccess(BuildConfig.REMOTE_ACCESS_PORT) }
        single { RemoteStatistics(BuildConfig.MAINTENANCE_PORT) }
        single { SambaClientWrapper(get()) }
        single { AuthPreferences(get()) }
        single { PathMovement() }
        single { UriUtils(get()) }
        single {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                get(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        single { (get() as Context).contentResolver }
        single { Gson() }
        single { AndroidWifiManager(get()) }
        single { WifiUtils(get()) }
    }

    private val viewModelModule = module {
        viewModel {
            if (true || shouldVerifyLocalNetwork() && !isInLocalNetwork(get())) {
                showToast(R.string.application_is_using_remote_access)
                RemoteAccessViewModel(get(), get(), get())
            } else {
                showToast(R.string.application_is_using_samba)
                SambaAccessViewModel(get(), get())
            }
        }
        viewModel { StatisticsViewModel(get()) }
        viewModel { UriViewModel(get()) }
    }

    private fun showToast(messageId: Int) {
        Toast.makeText(applicationContext, messageId, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@SambaClientApplication)
            modules(commonModule, viewModelModule)
        }
    }

    private fun shouldVerifyLocalNetwork() = localNetworkSsid != null && localNetworkSsid != ""

    private fun isInLocalNetwork(wifiUtils: WifiUtils): Boolean {
        val connectedSsid = wifiUtils.connectedSsid()?.replace("\"", "")
        return if (connectedSsid.isNullOrEmpty()) false
        else localNetworkSsid == connectedSsid
    }

    private val localNetworkSsid: String?
        get() = settingsPreferences.getString("local_network_ssid", null)

    private val settingsPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }
}
