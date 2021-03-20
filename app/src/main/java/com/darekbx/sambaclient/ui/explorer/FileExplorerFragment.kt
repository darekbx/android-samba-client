package com.darekbx.sambaclient.ui.explorer

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.AdapterSambaListFileBinding
import com.darekbx.sambaclient.databinding.AdapterSambaGridFileBinding
import com.darekbx.sambaclient.databinding.FragmentFileExplorerBinding
import com.darekbx.sambaclient.ui.explorer.SortingInfo.Companion.toSortingInfo
import com.darekbx.sambaclient.ui.samba.PathMovement
import com.darekbx.sambaclient.ui.samba.SambaFile
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * - sort order:
 *   - name (asc, desc), created date (asc, desc)
 *   - persisted
 * - open settings
 * - open maintentance
 *
 * - open file -> forward
 * - open dir -> forward (details: name, dates, size with files, like on google drive, delete with files)
 *
 * - add directory
 * - add file
 *
 */
class FileExplorerFragment : Fragment(R.layout.fragment_file_explorer) {

    companion object {
        private const val GRID_COLUMNS_COUNT = 4 // TODO from settings
        private const val IS_LIST = true // TODO from settings
        private const val BUNDLE_KEY = "eRn8Q5U5"
    }

    private val sambaViewModel: SambaViewModel by viewModel()
    private val pathMovement: PathMovement by inject()

    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var activeAdapter: BaseSambaFileAdapter
    private var sortingInfo = SortingInfo()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileExplorerBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(BUNDLE_KEY)?.let { sortingBundle ->
                sortingInfo = sortingBundle.toSortingInfo()
            }
        }

        initializeList(context)

        observeOnViewLifecycle(sambaViewModel.isLoading) { showHideLoadingLayout(it) }
        observeOnViewLifecycle(sambaViewModel.listResult) { handleListResult(it) }

        sambaViewModel.listDirectory(sortingInfo)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(BUNDLE_KEY, sortingInfo.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.explorer_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sort_options -> {
                openSortingDialog()
                true
            }
            R.id.menu_settings -> {
                findNavController().navigate(R.id.action_fileExplorerFragment_to_settingsFragment)
                true
            }
            R.id.menu_maintentance -> {
                findNavController().navigate(R.id.action_fileExplorerFragment_to_maintenanceFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSortingDialog() {
        val dialog = SortingDialog()
        dialog.arguments = sortingInfo.toBundle()
        dialog.onSortingInfo = {
            sortingInfo = this
            sambaViewModel.listDirectory(sortingInfo, pathMovement.currentPath())
        }
        dialog.show(childFragmentManager, SortingDialog::class.simpleName)
    }

    private fun initializeList(context: Context) {
        if (IS_LIST) {
            activeAdapter = filesListAdapter()
            binding.filesList.layoutManager = LinearLayoutManager(context)
        } else {
            activeAdapter = filesGridAdapter()
            binding.filesList.layoutManager = GridLayoutManager(context, GRID_COLUMNS_COUNT)
        }

        activeAdapter.onSambaFileClick = { handleSambaFileClick(it) }
        binding.filesList.adapter = activeAdapter
    }

    private fun handleListResult(resultWrapper: SambaViewModel.ResultWrapper<List<SambaFile>>) {
        if (resultWrapper.hasError) {
            pathMovement.removeLastPathSegment()
        } else {
            activeAdapter.swapData(resultWrapper.result)
        }
    }

    private fun openDirectory(directory: String?) {
        if (directory != null) {
            val path = pathMovement.obtainPath(directory)
            sambaViewModel.listDirectory(sortingInfo, path)
        }
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun handleSambaFileClick(sambaFile: SambaFile) {
        if (sambaFile.isDirectory) {
            openDirectory(sambaFile.name)
        } else {
            // TODO open file
        }
    }

    private fun filesListAdapter(): BaseSambaFileAdapter {
        return (object : SambaFileListAdapter<AdapterSambaListFileBinding>() {
            override fun inflateSambaFileView(
                inflater: LayoutInflater,
                root: ViewGroup
            ) = AdapterSambaListFileBinding.inflate(inflater, root, false)
        })
    }

    private fun filesGridAdapter(): BaseSambaFileAdapter {
        return object : SambaFileListAdapter<AdapterSambaGridFileBinding>() {
            override fun inflateSambaFileView(
                inflater: LayoutInflater,
                root: ViewGroup
            ) = AdapterSambaGridFileBinding.inflate(inflater, root, false)
        }
    }
}
