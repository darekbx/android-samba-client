package com.darekbx.sambaclient.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class LoadingViewModel: ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    protected fun <T> runIOInViewModelScope(callback: CoroutineScope.() -> T) {
        isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callback()
                isLoading.postValue(false)
            }
        }
    }
}
