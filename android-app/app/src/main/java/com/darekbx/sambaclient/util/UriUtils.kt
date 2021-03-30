package com.darekbx.sambaclient.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

class UriUtils(private val contentResolver: ContentResolver) {

    fun getUriFileName(uri: Uri): String? {
        val scheme = uri.getScheme()
        if (scheme == "file") {
            return uri.getLastPathSegment()
        } else if (scheme == "content") {
            val proj = arrayOf(MediaStore.Images.Media.TITLE)
            contentResolver.query(uri, proj, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }
}
