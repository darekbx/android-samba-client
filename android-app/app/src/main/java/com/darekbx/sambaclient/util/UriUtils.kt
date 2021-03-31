package com.darekbx.sambaclient.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.database.getStringOrNull
import com.darekbx.sambaclient.BuildConfig

class UriUtils(private val contentResolver: ContentResolver) {

    fun getUriFileName(uri: Uri): String? {
        val scheme = uri.scheme
        if (scheme == "file") {
            return uri.lastPathSegment
        } else if (scheme == "content") {
            return fetchByColumnName(uri, OpenableColumns.DISPLAY_NAME)
                ?: fetchByColumnName(uri, MediaStore.Images.Media.TITLE)
        }
        return null
    }

    private fun fetchByColumnName(uri: Uri, columnName: String): String? {
        val proj = arrayOf(columnName)
        try {
            contentResolver.query(uri, proj, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val titleIndex = cursor.getColumnIndexOrThrow(columnName)
                    return cursor.getStringOrNull(titleIndex)
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
        return null
    }
}
