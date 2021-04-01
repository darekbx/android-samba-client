package com.darekbx.sambaclient.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

open class LoadingViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    private val viewModelJob = SupervisorJob()
    protected val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    protected fun <T> runIOInViewModelScope(callback: suspend CoroutineScope.() -> T) {
        isLoading.postValue(true)
        ioScope.launch {
            callback()
            isLoading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
