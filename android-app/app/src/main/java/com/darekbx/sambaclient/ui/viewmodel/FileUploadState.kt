package com.darekbx.sambaclient.ui.viewmodel

import java.lang.Exception

class FileUploadState(
    val fileToUpload: FileToUpload,
    val uploaded: Boolean,
    val exception: Exception? = null
)
