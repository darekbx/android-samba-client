package com.darekbx.sambaclient.ui.viewmodel

import android.content.ContentResolver
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.ui.samba.Credentials
import com.darekbx.sambaclient.ui.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.samba.SambaFile
import com.darekbx.sambaclient.ui.viewmodel.model.FileToUpload
import com.darekbx.sambaclient.ui.viewmodel.model.FileUploadState
import com.darekbx.sambaclient.ui.viewmodel.model.ResultWrapper
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*

class SambaViewModel(
    private val sambaClientWrapper: SambaClientWrapper,
    private val contentResolver: ContentResolver
) : LoadingViewModel(), LifecycleObserver {

    companion object {
        private const val UPLOAD_ACTION_DELAY = 500L
    }

    val authenticationResult = MutableLiveData<ResultWrapper<Boolean>>()
    val autoAuthenticationResult = MutableLiveData<ResultWrapper<Boolean>>()
    val diskShareResult = MutableLiveData<ResultWrapper<Boolean>>()
    val listResult = MutableLiveData<ResultWrapper<List<SambaFile>>>()
    val fileInfoResult = MutableLiveData<ResultWrapper<SambaFile>>()
    val fileDownloadResult = MutableLiveData<ResultWrapper<String>>()
    val fileDeleteResult = MutableLiveData<ResultWrapper<Boolean>>()
    val directoryCreateResult = MutableLiveData<ResultWrapper<Boolean>>()
    val credentialsResult = MutableLiveData<ResultWrapper<Credentials>>()
    val fileUploadState = MutableLiveData<FileUploadState>()
    val fileUploadCompleted = MutableLiveData<Boolean>()

    fun authenticate(server: String, user: String? = null, password: String? = null, shareName: String) {
        runIOInViewModelScope {
            try {
                sambaClientWrapper.authenticate(server, user, password, shareName)
                autoAuthenticationResult.postValue(ResultWrapper(true))
            } catch (e: Exception) {
                e.printStackTrace()
                autoAuthenticationResult.postValue(ResultWrapper(e))
            }
        }
    }

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

    fun generateCredentialsMd5() {
        runIOInViewModelScope {
            try {
                val hostName = sambaClientWrapper.hostName()
                val md5Hash = sambaClientWrapper.generateCredentialsMd5()
                val credentials = Credentials(hostName, md5Hash)
                credentialsResult.postValue(ResultWrapper(credentials))
            } catch (e: Exception) {
                e.printStackTrace()
                credentialsResult.postValue(ResultWrapper(e))
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

    fun downloadFile(path: String) {
        val outFile = path.substringAfterLast(SambaClientWrapper.PATH_DELIMITER)
        val contentValues = contentValuesOf(
            MediaStore.Downloads.DISPLAY_NAME to outFile,
            MediaStore.Downloads.RELATIVE_PATH to Environment.DIRECTORY_DOWNLOADS
        )
        runIOInViewModelScope {
            try {
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

    fun uploadFiles(dirToUpload: String, filesToUpload: List<FileToUpload>) {
        runIOInViewModelScope {
            for (fileToUpload in filesToUpload) {
                try {
                    // Notify upload started
                    fileUploadState.postValue(FileUploadState(fileToUpload, false))
                    contentResolver.openInputStream(fileToUpload.uri)?.use { inStream ->
                        sambaClientWrapper.uploadFile(dirToUpload, fileToUpload.name, inStream)
                        // Notify upload completed, delay is for better ux experience
                        delay(UPLOAD_ACTION_DELAY)
                        fileUploadState.postValue(FileUploadState(fileToUpload, true))
                    } ?: throw IOException("Unable to open stream for $fileToUpload.name")
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Notify upload error, delay is for better ux experience
                    delay(UPLOAD_ACTION_DELAY)
                    fileUploadState.postValue(FileUploadState(fileToUpload, false, e))
                }
            }
            fileUploadCompleted.postValue(true)
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

    override fun onCleared() {
        super.onCleared()
        dispose()
    }
}
