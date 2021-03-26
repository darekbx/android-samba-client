package com.darekbx.sambaclient.ui.remotecontrol

class Statistics(
    val usedSpace: Long,
    val totalSpace: Long,
    val lastBackupTimestamp: Long,
    val biggestFiles: List<File>,
    val typeStatistics: List<TypeStatistic>
)

class File(val name: String, val directory: String, val size: Long)

class TypeStatistic(val fileType: String, val count: Int, val overallSize: Long) {

    companion object {
        val TYPE_IMAGES = "image"
        val TYPE_MOVIES = "movie"
        val TYPE_DOCUMENTS = "doc"
        val TYPE_ARCHIVES = "archive"
        val TYPE_OTHERS = "other"
    }
}
