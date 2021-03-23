package com.darekbx.sambaclient

import android.app.Application
import android.content.Context
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.ui.remotecontrol.RemoteControl
import com.darekbx.sambaclient.ui.samba.PathMovement
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.viewmodel.RemoteControlViewModel
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
        single { RemoteControl() }
        single { SambaClientWrapper(get()) }
        single { AuthPreferences(get()) }
        single { PathMovement() }
        single {
            (get() as Context).getSharedPreferences(
                "SambaClientApplication_preferences",
                Context.MODE_PRIVATE
            )
        }
        single { (get() as Context).contentResolver }
        single { Gson() }
    }

    private val viewModelModule = module {
        viewModel { SambaViewModel(get(), get()) }
        viewModel { RemoteControlViewModel(get()) }
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
