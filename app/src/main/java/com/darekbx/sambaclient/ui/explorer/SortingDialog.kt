package com.darekbx.sambaclient.ui.explorer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.ui.explorer.SortingInfo.Companion.toSortingInfo

class SortingDialog : DialogFragment() {

    var onSortingInfo: SortingInfo.() -> Unit = { }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_sort_order, null)
        val dialog = AlertDialog
            .Builder(context)
            .setView(layout)
            .setPositiveButton(R.string.sort_confirm, { _, _ -> saveSortingInformation() })
            .create()

        arguments?.toSortingInfo()?.let {
            applySortingInfo(it, layout)
        }

        return dialog
    }

    private fun applySortingInfo(it: SortingInfo, view: View) {
        val sortBy = if (it.isByName) R.id.sort_by_name else R.id.sort_by_time
        view.findViewById<RadioGroup>(R.id.sort_by_group).check(sortBy)

        val orderBy = if (it.isAscending) R.id.order_asc else R.id.order_desc
        view.findViewById<RadioGroup>(R.id.order_by_group).check(orderBy)
    }

    private fun saveSortingInformation() {
        dialog?.run {
            val sortByGroup = findViewById<RadioGroup>(R.id.sort_by_group)
            val orderByGroup = findViewById<RadioGroup>(R.id.order_by_group)

            val sortingInfo = SortingInfo(
                isByName = sortByGroup.checkedRadioButtonId == R.id.sort_by_name,
                isAscending = orderByGroup.checkedRadioButtonId == R.id.order_asc
            )

            onSortingInfo(sortingInfo)
        }
    }
}
