package com.darekbx.sambaclient.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.darekbx.sambaclient.ui.viewmodel.model.ResultWrapper
import com.darekbx.sambaclient.util.UriUtils
import java.lang.IllegalStateException

class UriViewModel(
    private val uriUtils: UriUtils
): LoadingViewModel()  {

    val fileNames = MutableLiveData<ResultWrapper<Map<Uri, String>>>()

    fun retrieveFileNames(uris: List<Uri>) {
        runIOInViewModelScope {
            try {
                val mappedNames = uris.associateWith { uri ->
                    uriUtils.getUriFileName(uri)
                        ?: throw IllegalStateException("Unable to retrieve file name")
                }
                fileNames.postValue(ResultWrapper(mappedNames))
            } catch (e: Exception) {
                fileNames.postValue(ResultWrapper(e))
            }
        }
    }
}
