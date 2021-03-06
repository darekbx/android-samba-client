package com.darekbx.sambaclient.viewmodel

import android.content.ContentResolver
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.remoteaccess.RemoteAccess
import com.darekbx.sambaclient.samba.Credentials
import com.darekbx.sambaclient.samba.SambaClientWrapper
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.util.toMd5
import com.darekbx.sambaclient.viewmodel.model.FileToUpload
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper

class RemoteAccessViewModel(
    private val remoteAccess: RemoteAccess,
    private val authPreferences: AuthPreferences,
    private val contentResolver: ContentResolver,
) : BaseAccessViewModel() {

    override fun authenticate(server: String, user: String?, password: String?, shareName: String) {
        runIOInViewModelScope {
            try {
                remoteAccess.setCredentials(server, "${user}_$password".toMd5(), server.toMd5())
                val result = remoteAccess.authorize()
                autoAuthenticationResult.postValue(ResultWrapper(result.authorized))
            } catch (e: Exception) {
                e.printStackTrace()
                autoAuthenticationResult.postValue(ResultWrapper(e))
            }
        }
    }

    override fun authenticate(server: String, user: String?, password: String?) {
        this.authenticate(server, user, password, "")
    }

    override fun generateCredentialsMd5() {
        runIOInViewModelScope {
            try {
                val storedCredentials = authPreferences.read()
                val hostName = storedCredentials.address ?: ""
                val md5Hash = "${storedCredentials.user}_${storedCredentials.password}"
                val credentials = Credentials(hostName, md5Hash)
                credentialsResult.postValue(ResultWrapper(credentials))
            } catch (e: Exception) {
                e.printStackTrace()
                credentialsResult.postValue(ResultWrapper(e))
            }
        }
    }

    override fun connectToDiskShare(shareName: String) {
        diskShareResult.postValue(ResultWrapper(true))
    }

    override fun listDirectory(sortingInfo: SortingInfo, directory: String) {
        runIOInViewModelScope {
            try {
                val list = remoteAccess.list(directory)
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
                val sambaFile = remoteAccess.fileDetails(path)
                fileInfoResult.postValue(ResultWrapper(sambaFile))
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
                    Log.v("-------", "write start out")
                    contentResolver.openOutputStream(uri)?.use { outStream ->
                        remoteAccess.fileDownload(path, outStream)
                        Log.v("-------", "write end out in")
                    }
                    Log.v("-------", "write end out")
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
                val commonResult = remoteAccess.fileDelete(path)
                fileDeleteResult.postValue(ResultWrapper(commonResult.result))
            } catch (e: Exception) {
                e.printStackTrace()
                fileDeleteResult.postValue(ResultWrapper(e))
            }
        }
    }

    override fun createDirectory(path: String, directoryName: String) {

    }

    override fun uploadFiles(dirToUpload: String, filesToUpload: List<FileToUpload>) {

    }
}
