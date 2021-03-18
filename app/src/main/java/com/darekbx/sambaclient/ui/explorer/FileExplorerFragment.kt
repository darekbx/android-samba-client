package com.darekbx.sambaclient.ui.explorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentFileExplorerBinding
import com.darekbx.sambaclient.ui.samba.SambaFile
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * - navigate between directories
 * - two views:
 *   - list
 *   - grid
 *
 */
class FileExplorerFragment: Fragment(R.layout.fragment_file_explorer) {

    private val sambaViewModel: SambaViewModel by viewModel()

    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filesList.layoutManager = LinearLayoutManager(requireContext())
        binding.filesList.adapter = filesAdapter

        observeOnViewLifecycle(sambaViewModel.isLoading) { showHideLoadingLayout(it) }
        observeOnViewLifecycle(sambaViewModel.listResult) { handleListResult(it) }
        sambaViewModel.listDirectory()
    }

    private fun handleListResult(resultWrapper: SambaViewModel.ResultWrapper<List<SambaFile>>) {
        if (resultWrapper.hasError) {

            // TODO show error

        } else {
            filesAdapter.swapData(resultWrapper.result)
        }
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private val filesAdapter by lazy { SambaFileAdapter() }
}
