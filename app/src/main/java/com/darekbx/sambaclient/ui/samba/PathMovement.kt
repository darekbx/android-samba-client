package com.darekbx.sambaclient.ui.samba

class PathMovement {

    private val pathSegments = mutableListOf<String>()


    fun currentPath() = pathSegments.joinToString("/")

    fun obtainPath(directory: String): String {
        when (directory) {
            "." -> removeLastPathSegment()
            ".." -> pathSegments.clear()
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
