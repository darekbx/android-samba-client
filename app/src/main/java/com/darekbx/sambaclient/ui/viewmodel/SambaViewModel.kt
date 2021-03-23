package com.darekbx.sambaclient.ui.viewmodel

import android.content.ContentResolver
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.samba.SambaFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.*
import kotlin.Comparator

class SambaViewModel(
    private val sambaClientWrapper: SambaClientWrapper,
    private val contentResolver: ContentResolver
) : ViewModel(), LifecycleObserver {

    class ResultWrapper<T>(val result: T?, val exception: Exception? = null) {
        val hasError = exception != null

        constructor(exception: Exception) : this(null, exception)

        val errorMessage = exception?.message
    }

    val authenticationResult = MutableLiveData<ResultWrapper<Boolean>>()
    val diskShareResult = MutableLiveData<ResultWrapper<Boolean>>()
    val listResult = MutableLiveData<ResultWrapper<List<SambaFile>>>()
    val fileInfoResult = MutableLiveData<ResultWrapper<SambaFile>>()
    val fileDownloadResult = MutableLiveData<ResultWrapper<String>>()
    val fileDeleteResult = MutableLiveData<ResultWrapper<Boolean>>()
    val directoryCreateResult = MutableLiveData<ResultWrapper<Boolean>>()
    val isLoading = MutableLiveData<Boolean>()

    fun authenticate(server: String, user: String? = null, password: String? = null) {
        runIOInViewModelScope {
            try {
                sambaClientWrapper.authenticate(server, user, password)
                authenticationResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                e.printStackTrace()
                authenticationResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun connectToDiskShare(shareName: String) {
        runIOInViewModelScope {
            try {
                sambaClientWrapper.connectToDiskShare(shareName)
                diskShareResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                e.printStackTrace()
                diskShareResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun listDirectory(sortingInfo: SortingInfo, directory: String = "") {
        runIOInViewModelScope {
            try {
                val list = sambaClientWrapper.list(directory)
                val comparator = createComparator(sortingInfo)
                val sortedList = list.sortedWith(comparator)
                listResult.postValue(ResultWrapper(sortedList))
            } catch (e: Exception) {
                e.printStackTrace()
                listResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun fileInfo(path: String) {
        runIOInViewModelScope {
            try {
                val fileInfo = sambaClientWrapper.fileInformation(path)
                fileInfoResult.postValue(ResultWrapper(fileInfo))
            } catch (e: Exception) {
                e.printStackTrace()
                fileInfoResult.postValue(ResultWrapper(e))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadFile(path: String) {
        val outFile = path.substringAfterLast(SambaClientWrapper.PATH_DELIMITER)
        val contentValues = contentValuesOf(
            MediaStore.Downloads.DISPLAY_NAME to outFile,
            MediaStore.Downloads.RELATIVE_PATH to Environment.DIRECTORY_DOWNLOADS
        )
        runIOInViewModelScope {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    throw IllegalStateException("Not supported for Android below 10")
                }
                contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )?.let { uri ->
                    contentResolver.openOutputStream(uri)?.use { outStream ->
                        sambaClientWrapper.fileDownload(path, outStream)
                    }
                    fileDownloadResult.postValue(ResultWrapper("$uri"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fileDownloadResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun deleteFile(path: String) {
        runIOInViewModelScope {
            try {
                sambaClientWrapper.fileDelete(path)
                fileDeleteResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                e.printStackTrace()
                fileDeleteResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun createDirectory(path: String, directoryName: String) {
        runIOInViewModelScope {
            try {
                sambaClientWrapper.createDirectory(path, directoryName)
                directoryCreateResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                e.printStackTrace()
                directoryCreateResult.postValue(ResultWrapper(e))
            }
        }
    }

    private fun createComparator(sortingInfo: SortingInfo): Comparator<SambaFile> {
        return compareByDescending<SambaFile> { it.isUpMark }
            .thenByDescending { it.isDirectory }
            .run {
                when (sortingInfo.isByName) {
                    true -> when (sortingInfo.isAscending) {
                        true -> thenBy { it.name.toLowerCase(Locale.getDefault()) }
                        else -> thenByDescending { it.name.toLowerCase(Locale.getDefault()) }
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
        isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callback()
                isLoading.postValue(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
    }
}
