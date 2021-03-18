package com.darekbx.sambaclient

import android.app.Application
import android.content.Context
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.hierynomus.smbj.SMBClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class SambaClientApplication : Application() {

    private val commonModule = module {
        single { SMBClient() }
        single { SambaClientWrapper(get()) }
        single { AuthPreferences(get()) }
        single {
            (get() as Context).getSharedPreferences(
                "SambaClientApplication_preferences",
                Context.MODE_PRIVATE
            )
        }
    }

    private val viewModelModule = module {
        viewModel { SambaViewModel(get()) }
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
