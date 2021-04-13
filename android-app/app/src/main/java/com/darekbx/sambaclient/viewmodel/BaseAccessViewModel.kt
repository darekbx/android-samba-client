package com.darekbx.sambaclient.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.darekbx.sambaclient.samba.Credentials
import com.darekbx.sambaclient.samba.SambaFile
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.viewmodel.model.FileToUpload
import com.darekbx.sambaclient.viewmodel.model.FileUploadState
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import java.util.*

abstract class BaseAccessViewModel : LoadingViewModel(), LifecycleObserver {

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

    abstract fun authenticate(
        server: String,
        user: String? = null,
        password: String? = null,
        shareName: String
    )

    abstract fun authenticate(server: String, user: String?, password: String?)
    abstract fun generateCredentialsMd5()
    abstract fun connectToDiskShare(shareName: String)
    abstract fun listDirectory(sortingInfo: SortingInfo, directory: String)
    abstract fun fileInfo(path: String)
    abstract fun downloadFile(path: String)
    abstract fun deleteFile(path: String)
    abstract fun createDirectory(path: String, directoryName: String)
    abstract fun uploadFiles(dirToUpload: String, filesToUpload: List<FileToUpload>)


    protected fun createComparator(sortingInfo: SortingInfo): Comparator<SambaFile> {
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
}
