package com.darekbx.sambaclient.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.TITLE
import androidx.lifecycle.MutableLiveData

class UriViewModel(
    private val contentResolver: ContentResolver
): LoadingViewModel()  {

    val fileNames = MutableLiveData<List<String>>()

    fun retrieveFileNames(uris: List<Uri>) {
        runIOInViewModelScope {
            val names = uris.mapNotNull { getUriFileName(it) }
            fileNames.postValue(names)
        }
    }

    private fun getUriFileName(uri: Uri): String? {
        val scheme = uri.getScheme()
        if (scheme == "file") {
            return uri.getLastPathSegment()
        } else if (scheme == "content") {
            val proj = arrayOf(MediaStore.Images.Media.TITLE)
            contentResolver.query(uri, proj, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex: Int = cursor.getColumnIndexOrThrow(TITLE)
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }
}
