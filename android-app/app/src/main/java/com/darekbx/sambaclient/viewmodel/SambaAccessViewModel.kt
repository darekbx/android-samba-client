package com.darekbx.sambaclient.viewmodel

import android.content.ContentResolver
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.samba.Credentials
import com.darekbx.sambaclient.samba.SambaClientWrapper
import com.darekbx.sambaclient.viewmodel.model.FileToUpload
import com.darekbx.sambaclient.viewmodel.model.FileUploadState
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import kotlinx.coroutines.delay
import java.io.IOException

class SambaAccessViewModel(
    private val sambaClientWrapper: SambaClientWrapper,
    private val contentResolver: ContentResolver
) : BaseAccessViewModel() {

    companion object {
        private const val UPLOAD_ACTION_DELAY = 500L
    }

    override fun authenticate(
        server: String,
        user: String?,
        password: String?,
        shareName: String
    ) {
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

    override fun authenticate(server: String, user: String?, password: String?) {
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

    override fun generateCredentialsMd5() {
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

    override fun connectToDiskShare(shareName: String) {
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

    override fun listDirectory(sortingInfo: SortingInfo, directory: String) {
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

    override fun fileInfo(path: String) {
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

    override fun downloadFile(path: String) {
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

    override fun deleteFile(path: String) {
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

    override fun createDirectory(path: String, directoryName: String) {
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

    override fun uploadFiles(dirToUpload: String, filesToUpload: List<FileToUpload>) {
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
