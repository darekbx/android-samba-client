package com.darekbx.sambaclient.ui.viewmodel

import androidx.lifecycle.*
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.samba.SambaFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SambaViewModel(
    private val sambaClientWrapper: SambaClientWrapper
) : ViewModel(), LifecycleObserver {

    class ResultWrapper<T>(val result: T, val exception: Exception? = null) {
        val hasError = exception != null
    }

    val authenticationResult = MutableLiveData<ResultWrapper<Boolean>>()
    val diskShareResult = MutableLiveData<ResultWrapper<Boolean>>()
    val listResult = MutableLiveData<ResultWrapper<List<SambaFile>>>()
    val isLoading = MutableLiveData<Boolean>()

    fun dispose() {
        runIOInViewModelScope {
            sambaClientWrapper.close()
        }
    }

    fun authenticate(server: String, user: String? = null, password: String? = null) {
        isLoading.postValue(true)
        runIOInViewModelScope {
            try {
                sambaClientWrapper.authenticate(server, user, password)
                authenticationResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                authenticationResult.postValue(ResultWrapper(false, e))
            }
            isLoading.postValue(false)
        }
    }

    fun connectToDiskShare(shareName: String) {
        isLoading.postValue(true)
        runIOInViewModelScope {
            try {
                sambaClientWrapper.connectToDiskShare(shareName)
                diskShareResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                diskShareResult.postValue(ResultWrapper(false, e))
            }
            isLoading.postValue(false)
        }
    }

    fun listDirectory(directory: String = "") {
        runIOInViewModelScope {
            try {
                val list = sambaClientWrapper.list(directory)
                listResult.postValue(ResultWrapper(list))
            } catch (e: Exception) {
                listResult.postValue(ResultWrapper(emptyList(), e))
            }
        }
    }

    private fun <T> runIOInViewModelScope(callback: CoroutineScope.() -> T) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callback()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
    }

    /*fun test() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val smbClient = SMBClient()
                val connection = smbClient.connect("192.168.0.27")

                val session = connection.authenticate(
                    AuthenticationContext(
                        "samba",
                        "****".toCharArray(),
                        null
                    )
                )

                val share = session.connectShare("timemachine")
                val diskShare = share as DiskShare
                diskShare.list("laptopy").forEach {
                    Log.v("--------", it.fileName)
                }

                session.close()
                connection.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/
}
