package com.darekbx.sambaclient.ui.samba

class PathMovement {

    private val pathSegments = mutableListOf<String>()

    companion object {
        const val UP_SEGMENT = "."
        const val ROOT_SEGMENT = ".."
    }

    fun depth() = pathSegments.size

    fun currentPath() = pathSegments.joinToString("/")

    fun obtainPath(directory: String): String {
        when (directory) {
            UP_SEGMENT -> removeLastPathSegment()
            ROOT_SEGMENT -> pathSegments.clear()
            else -> pathSegments.add(directory)
        }
        return currentPath()
    }

    fun removeLastPathSegment() {
        if (pathSegments.isNotEmpty()) {
            pathSegments.removeLast()
        }
    }

    fun clear() {
        pathSegments.clear()
    }
}
