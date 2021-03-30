package com.darekbx.sambaclient.ui.share

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.DialogDirectorySelectBinding
import com.darekbx.sambaclient.ui.explorer.SortingInfo
import com.darekbx.sambaclient.ui.samba.PathMovement
import com.darekbx.sambaclient.ui.samba.SambaFile
import com.darekbx.sambaclient.ui.viewmodel.ResultWrapper
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.ui.viewmodel.UriViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class DirectorySelectDialog : DialogFragment() {

    private val pathMovement: PathMovement by inject()
    private val sambaViewModel: SambaViewModel by viewModel()

    private var _binding: DialogDirectorySelectBinding? = null
    private val binding get() = _binding!!

    var onSelect: (name: String) -> Unit = { }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDirectorySelectBinding.inflate(layoutInflater)
        val dialog = AlertDialog
            .Builder(context)
            .setView(binding.root)
            .setPositiveButton(R.string.select_directory_create) { _, _ ->
                onSelect(pathMovement.currentPath())
            }
            .create()

        initializeList()

        with(sambaViewModel) {
            isLoading.observe(this@DirectorySelectDialog) { showHideLoadingLayout(it) }
            listResult.observe(this@DirectorySelectDialog) { handleListResult(it) }
        }

        sambaViewModel.listDirectory(sortingInfo, pathMovement.currentPath())

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeList() {
        binding.directoryList.adapter = adapter
        binding.directoryList.setOnItemClickListener { _, _, position, _ ->
            val directory = adapter.getItem(position)
            if (directory != null) {
                openDirectory(directory.name)
            }
        }
    }

    private fun handleListResult(resultWrapper: ResultWrapper<List<SambaFile>>) {
        if (resultWrapper.hasError) {
            pathMovement.removeLastPathSegment()
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_SHORT).show()
        } else {
            val onlyDirectories = resultWrapper.requireResult().filter { it.isDirectory }
            adapter.clear()
            adapter.addAll(onlyDirectories)

            binding.directoryList.setSelectionAfterHeaderView()
            binding.currentDirectory.text = pathMovement.currentPath()
        }
    }

    private fun openDirectory(directory: String?) {
        if (directory != null) {
            val path = pathMovement.obtainPath(directory)
            sambaViewModel.listDirectory(sortingInfo, path)
        }
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        val loadingLayout = dialog?.findViewById<View>(R.id.loading_layout)
        loadingLayout?.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private val adapter by lazy { DirectorySelectAdapter(requireContext()) }
    private val sortingInfo by lazy { SortingInfo() }
}
