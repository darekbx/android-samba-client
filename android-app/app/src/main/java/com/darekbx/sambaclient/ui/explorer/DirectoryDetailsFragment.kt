package com.darekbx.sambaclient.ui.explorer

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentDirectoryDetailsBinding
import com.darekbx.sambaclient.samba.Credentials
import com.darekbx.sambaclient.statistics.SubDirStatistics
import com.darekbx.sambaclient.viewmodel.StatisticsViewModel
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import com.darekbx.sambaclient.util.setDateTime
import com.darekbx.sambaclient.viewmodel.BaseAccessViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class DirectoryDetailsFragment : Fragment(R.layout.fragment_directory_details) {

    companion object {
        private const val LOADING_HIDE_DELAY = 1000L
    }

    private val accessViewModel: BaseAccessViewModel by viewModel()
    private val statisticsViewModel: StatisticsViewModel by viewModel()

    private var _binding: FragmentDirectoryDetailsBinding? = null
    private val binding get() = _binding!!

    private var subDir = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectoryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(accessViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(credentialsResult) { handleMd5Credentials(it) }
        }
        with(statisticsViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(subDirStatisticsResult) { handleSubDirStatistics(it) }
        }
    }

    private fun handleMd5Credentials(resultWrapper: ResultWrapper<Credentials>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {
            val credentials = resultWrapper.requireResult()
            statisticsViewModel.retrieveStatistics(
                credentials.hostname, credentials.md5Credentials, subDir
            )
        }
    }

    private fun handleSubDirStatistics(resultWrapper: ResultWrapper<SubDirStatistics>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {
            with(resultWrapper.requireResult()) {
                binding.dirName.text = getString(R.string.directory_details_dir_format, subDir)
                binding.usedSpace.text = Formatter.formatFileSize(requireContext(), usedSpace)
                binding.counts.text =
                    getString(R.string.directory_details_counts_format, directoryCount, filesCount)
                binding.createdTime.setDateTime(createdTime)
                binding.modifiedTime.setDateTime(modifiedTime)
            }
        }
    }

    fun loadDetailsForDirectory(directory: String) {
        clear()
        accessViewModel.generateCredentialsMd5()
        subDir = directory
    }

    private fun clear() {
        binding.dirName.text = "-"
        binding.usedSpace.text = "-"
        binding.counts.text = "- / -"
        binding.createdTime.text = "-"
        binding.modifiedTime.text = "-"
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        val layout = binding.loadingLayout.loadingLayout
        if (isLoading) {
            layout.visibility = View.VISIBLE
        } else {
            layout.postDelayed({ layout.visibility = View.GONE }, LOADING_HIDE_DELAY)
        }
    }
}
