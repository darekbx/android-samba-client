package com.darekbx.sambaclient

import android.app.Application
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.ui.statistics.RemoteStatistics
import com.darekbx.sambaclient.ui.samba.PathMovement
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.viewmodel.StatisticsViewModel
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
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
        single { RemoteStatistics(BuildConfig.REMOTE_CONTROL_PORT) }
        single { SambaClientWrapper(get()) }
        single { AuthPreferences(get()) }
        single { PathMovement() }
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
    }

    private val viewModelModule = module {
        viewModel { SambaViewModel(get(), get()) }
        viewModel { StatisticsViewModel(get()) }
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
}
