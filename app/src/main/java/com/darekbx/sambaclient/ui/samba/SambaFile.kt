package com.darekbx.sambaclient.ui.samba

import com.hierynomus.msfscc.FileAttributes

class SambaFile(
    val fileName: String,
    val creationTime: Long,
    val changeTime: Long,
    val size: Long,
    private val attributes: Long
) {

    val isDirectory = attributes == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value
}
