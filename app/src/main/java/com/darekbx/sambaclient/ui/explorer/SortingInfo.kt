package com.darekbx.sambaclient.ui.explorer

import android.os.Bundle

class SortingInfo(val isByName: Boolean = true, val isAscending: Boolean = true) {

    companion object {
        private const val BY_NAME_KEY = "3nPB7juS"
        private const val ASCENING_KEY = "VR8GHhMH"

        fun Bundle.toSortingInfo() =
            SortingInfo(
                isByName = getBoolean(BY_NAME_KEY),
                isAscending = getBoolean(ASCENING_KEY)
            )
    }

    fun toBundle() =
        Bundle().apply {
            putBoolean(BY_NAME_KEY, isByName)
            putBoolean(ASCENING_KEY, isAscending)
        }
}
