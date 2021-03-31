package com.darekbx.sambaclient.ui.viewmodel

import java.lang.Exception

class FileUploadState(
    val fileToUpload: FileToUpload,
    var uploaded: Boolean? = null,
    var exception: Exception? = null
) {

    fun equalsByUri(other: FileUploadState): Boolean {
        return this.fileToUpload.uri == other.fileToUpload.uri
    }

    fun errorImageInvisible(): Boolean = exception == null
    fun uploadedImageInvisible(): Boolean = uploaded != true
    fun progressInvisible(): Boolean = uploaded == true || exception != null
}
