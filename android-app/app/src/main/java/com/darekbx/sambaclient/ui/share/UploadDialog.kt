package com.darekbx.sambaclient.ui.share

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.DialogUploadBinding
import com.darekbx.sambaclient.viewmodel.model.FileToUpload
import com.darekbx.sambaclient.viewmodel.model.FileUploadState

class UploadDialog : DialogFragment() {

    private var _binding: DialogUploadBinding? = null
    private val binding get() = _binding!!

    var onDismissed: () -> Unit = { }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogUploadBinding.inflate(layoutInflater)
        val dialog = AlertDialog
            .Builder(context)
            .setView(binding.root)
            .create()

        binding.filesList.layoutManager = LinearLayoutManager(context)
        binding.filesList.adapter = uploadStateAdapter
        binding.closeButton.setOnClickListener {
            onDismissed()
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun notifyUploadCompleted() {
        binding.closeButton.isEnabled = true
        binding.title.setText(R.string.upload_completed)
    }

    fun addStates(filesToUpload: List<FileToUpload>) {
        val uploadStates = filesToUpload.map { FileUploadState(it) }
        uploadStateAdapter.fillData(uploadStates)
    }

    fun updateState(fileUploadState: FileUploadState) {
        uploadStateAdapter.update(fileUploadState)
    }

    private val uploadStateAdapter by lazy { UploadStateAdapter() }
}
