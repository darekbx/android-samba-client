package com.darekbx.sambaclient.ui.samba

import com.darekbx.sambaclient.R
import com.hierynomus.msfscc.FileAttributes
import java.io.File

class SambaFile(
    val name: String,
    val creationTime: Long,
    val changeTime: Long,
    val size: Long,
    private val attributes: Long
) {

    val isDirectory = attributes == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value
    val isUpMark = name == "." || name == ".."

    val icon = when (File(name).extension.toLowerCase()) {
        "jpg", "png", "jpeg", "bmp", "svg", "gif" -> R.drawable.ic_file_image
        "doc", "docx", "txt", "rtf" -> R.drawable.ic_text_file
        "zip", "gz", "rar", "tar", "7z" -> R.drawable.ic_archive_file
        else -> R.drawable.ic_common_file
    }
}
