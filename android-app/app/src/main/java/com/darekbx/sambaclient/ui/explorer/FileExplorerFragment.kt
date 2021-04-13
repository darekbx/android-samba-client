package com.darekbx.sambaclient.ui.explorer

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.darekbx.sambaclient.BuildConfig
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.AdapterSambaListFileBinding
import com.darekbx.sambaclient.databinding.AdapterSambaGridFileBinding
import com.darekbx.sambaclient.databinding.FragmentFileExplorerBinding
import com.darekbx.sambaclient.ui.explorer.SortingInfo.Companion.toSortingInfo
import com.darekbx.sambaclient.samba.PathMovement
import com.darekbx.sambaclient.samba.SambaFile
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import com.darekbx.sambaclient.viewmodel.BaseAccessViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class FileExplorerFragment :
    Fragment(R.layout.fragment_file_explorer), DrawerLayout.DrawerListener {

    companion object {
        private const val SORT_INFO_BUNDLE_KEY = "eRn8Q5U5"
        const val FILE_PATH_KEY = "83RcSgXp"
    }

    private val accessViewModel: BaseAccessViewModel by viewModel()
    private val pathMovement: PathMovement by inject()

    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var activeAdapter: BaseSambaFileAdapter
    private var sortingInfo = SortingInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (closeDrawer()) return
                if (!goUp()) {
                    // Went to the root, close app
                    requireActivity().finish()
                }
            }
        })
    }

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
            savedInstanceState.getBundle(SORT_INFO_BUNDLE_KEY)?.let { sortingBundle ->
                sortingInfo = sortingBundle.toSortingInfo()
            }
        }

        initializeList(context)

        observeOnViewLifecycle(accessViewModel.isLoading) { showHideLoadingLayout(it) }
        observeOnViewLifecycle(accessViewModel.listResult) { handleListResult(it) }
        observeOnViewLifecycle(accessViewModel.directoryCreateResult) { handleCreateDirResult(it) }

        accessViewModel.listDirectory(sortingInfo, pathMovement.currentPath())

        binding.buttonAdd.setOnClickListener { openCreateDirectoryDialog() }
        binding.root.addDrawerListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(SORT_INFO_BUNDLE_KEY, sortingInfo.toBundle())
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

    override fun onDrawerOpened(drawerView: View) {
        refreshDirectoryDetails()
    }

    override fun onDrawerClosed(drawerView: View) { }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }

    override fun onDrawerStateChanged(newState: Int) { }

    private fun openSortingDialog() {
        val dialog = SortingDialog()
        dialog.arguments = sortingInfo.toBundle()
        dialog.onSortingInfo = {
            sortingInfo = this
            accessViewModel.listDirectory(sortingInfo, pathMovement.currentPath())
        }
        dialog.show(childFragmentManager, SortingDialog::class.simpleName)
    }

    private fun openCreateDirectoryDialog() {
        val dialog = CreateDirectoryDialog()
        dialog.arguments = sortingInfo.toBundle()
        dialog.onCreate = { directoryName ->
            accessViewModel.createDirectory(pathMovement.currentPath(), directoryName)
        }
        dialog.show(childFragmentManager, CreateDirectoryDialog::class.simpleName)
    }

    private fun initializeList(context: Context) {
        if (showAsList) {
            activeAdapter = filesListAdapter()
            binding.filesList.layoutManager = LinearLayoutManager(context)
        } else {
            activeAdapter = filesGridAdapter()
            binding.filesList.layoutManager = GridLayoutManager(context, gridColumnsCount)
        }

        activeAdapter.onSambaFileClick = { handleSambaFileClick(it) }
        binding.filesList.adapter = activeAdapter
    }

    private fun handleCreateDirResult(resultWrapper: ResultWrapper<Boolean>) {
        if (resultWrapper.hasError) {
            pathMovement.removeLastPathSegment()
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_SHORT)
                .show()
        } else {
            // Refresh after dir creation
            accessViewModel.listDirectory(sortingInfo, pathMovement.currentPath())
        }
    }

    private fun handleListResult(resultWrapper: ResultWrapper<List<SambaFile>>) {
        if (resultWrapper.hasError) {
            pathMovement.removeLastPathSegment()
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_SHORT)
                .show()
        } else {
            scrollListToTop()
            activeAdapter.swapData(resultWrapper.requireResult())
        }
    }

    private fun refreshDirectoryDetails() {
        childFragmentManager.fragments.find { it is DirectoryDetailsFragment }?.let { fragment ->
            val currentPath = pathMovement.currentPath()
            (fragment as DirectoryDetailsFragment).loadDetailsForDirectory(currentPath)
        }
    }

    private fun scrollListToTop() {
        binding.filesList.scrollToPosition(0)
    }

    private fun goUp(): Boolean {
        if (pathMovement.depth() == 0) return false
        pathMovement.removeLastPathSegment()
        accessViewModel.listDirectory(sortingInfo, pathMovement.currentPath())
        return true
    }

    private fun openDirectory(directory: String?) {
        if (directory != null) {
            val path = pathMovement.obtainPath(directory)
            accessViewModel.listDirectory(sortingInfo, path)
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
            val pathToFile =
                if (pathMovement.depth() == 0) sambaFile.name
                else "${pathMovement.currentPath()}/${sambaFile.name}"
            val arguments = Bundle().apply {
                putString(FILE_PATH_KEY, pathToFile)
            }

            findNavController()
                .navigate(R.id.action_fileExplorerFragment_to_fileFragment, arguments)
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

    private fun closeDrawer(): Boolean {
        if (binding.root.isDrawerOpen(GravityCompat.END)) {
            binding.root.closeDrawers()
            return true
        }
        return false
    }

    private val showAsList: Boolean
        get() = settingsPreferences.getBoolean("show_as_list", BuildConfig.SHOW_AS_LIST)

    private val gridColumnsCount: Int
        get() = settingsPreferences.getInt("grid_columns_count", BuildConfig.GRID_COLUMNS_COUNT)

    private val settingsPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
}
