package com.darekbx.sambaclient.ui.viewmodel

import androidx.lifecycle.*
import com.darekbx.sambaclient.ui.explorer.SortingInfo
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

    fun listDirectory(sortingInfo: SortingInfo, directory: String = "") {
        runIOInViewModelScope {
            try {
                val list = sambaClientWrapper.list(directory)
                var comparator = createComparator(sortingInfo)
                val sortedList = list.sortedWith(comparator)
                listResult.postValue(ResultWrapper(sortedList))
            } catch (e: Exception) {
                listResult.postValue(ResultWrapper(emptyList(), e))
            }
        }
    }

    private fun createComparator(sortingInfo: SortingInfo): Comparator<SambaFile> {
        return compareByDescending<SambaFile> { it.isUpMark }
            .thenByDescending { it.isDirectory }
            .run {
                when (sortingInfo.isByName) {
                    true -> when (sortingInfo.isAscending) {
                        true -> thenBy { it.name.toLowerCase() }
                        else -> thenByDescending { it.name.toLowerCase() }
                    }
                    else -> when (sortingInfo.isAscending) {
                        true -> thenBy { it.changeTime }
                        else -> thenByDescending { it.changeTime }
                    }
                }
            }
    }

    private fun dispose() {
        runIOInViewModelScope {
            sambaClientWrapper.close()
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
}
