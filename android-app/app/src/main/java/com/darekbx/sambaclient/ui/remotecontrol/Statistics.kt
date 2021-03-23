package com.darekbx.sambaclient.ui.remotecontrol

class Statistics(
    val usedSpace: Long,
    val freeSpace: Long,
    val lastBackupTimestamp: Long,
    val biggestFiles: List<File>,
    val typeStatistics: List<TypeStatistic>
)

class File(val name: String, val directory: String, val size: Long)

class TypeStatistic(val fileType: Int, val count: Int, val overallSize: Long)
