package com.darekbx.sambaclient.ui.explorer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.darekbx.sambaclient.R

class CreateDirectoryDialog : DialogFragment() {

    var onCreate: (name: String) -> Unit = { }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_create_directory, null)
        val dialog = AlertDialog
            .Builder(context)
            .setView(layout)
            .setNegativeButton(R.string.create_directory_cancel, null)
            .setPositiveButton(R.string.create_directory_create, { _, _ ->
                dialog?.findViewById<EditText>(R.id.directory_name)?.run {
                    onCreate(text.toString())
                }
            })
            .create()

        return dialog
    }
}
