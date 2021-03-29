package com.darekbx.sambaclient.ui.maintenance

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentMaintenanceBinding
import com.darekbx.sambaclient.ui.statistics.Statistics
import com.darekbx.sambaclient.ui.statistics.TypeStatistic
import com.darekbx.sambaclient.ui.samba.Credentials
import com.darekbx.sambaclient.ui.viewmodel.StatisticsViewModel
import com.darekbx.sambaclient.ui.viewmodel.ResultWrapper
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * TODO
 * - last backup date
 * - make backup (select usb drive for a backup, detect usb drives)
 */
class MaintenanceFragment: Fragment() {

    private val sambaViewModel: SambaViewModel by viewModel()
    private val statisticsViewModel: StatisticsViewModel by viewModel()

    private var _binding: FragmentMaintenanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(sambaViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(credentialsResult) { handleMd5Credentials(it) }

            generateCredentialsMd5()
        }

        with(statisticsViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(statisticsResult) { handleStatistics(it) }
        }
    }

    private fun handleMd5Credentials(resultWrapper: ResultWrapper<Credentials>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {
            val credentials = resultWrapper.requireResult()
            statisticsViewModel.retrieveStatistics(credentials.hostname, credentials.md5Credentials)
        }
    }

    private fun handleStatistics(resultWrapper: ResultWrapper<Statistics>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {

            with(resultWrapper.requireResult()) {
                displayUsedSpace(this)
                displayTypeStatistics(this)
                displayBiggestFiles(this)
            }
        }
    }

    private fun displayUsedSpace(statistics: Statistics) {
        val usedPercent = (statistics.usedSpace * 100 / statistics.totalSpace).toInt()
        binding.userSpaceProgress.max = 100
        binding.userSpaceProgress.progress = usedPercent

        val userSpaceFormatted = Formatter.formatFileSize(requireContext(), statistics.usedSpace)
        val totalSpaceFormatted = Formatter.formatFileSize(requireContext(), statistics.totalSpace)

        binding.usedSpace.text = getString(
            R.string.statistics_used_of, userSpaceFormatted, totalSpaceFormatted
        )
    }

    private fun displayTypeStatistics(statistics: Statistics) {
        binding.countChart.invalidateWithData(statistics.typeStatistics)
        binding.sizeChart.invalidateWithData(statistics.typeStatistics)

        with(statistics) {
            displayLegend(binding.typeImages, TypeStatistic.TYPE_IMAGES, R.string.type_image)
            displayLegend(binding.typeMovies, TypeStatistic.TYPE_MOVIES, R.string.type_movie)
            displayLegend(binding.typeDocs, TypeStatistic.TYPE_DOCUMENTS, R.string.type_doc)
            displayLegend(binding.typeArchives, TypeStatistic.TYPE_ARCHIVES, R.string.type_archive)
            displayLegend(binding.typeOthers, TypeStatistic.TYPE_OTHERS, R.string.type_other)
        }
    }

    private fun Statistics.displayLegend(view: TextView, type: String, messageResId: Int) {
        val imagesType = typeStatistics.first { it.fileType == type }
        view.text = getString(
            messageResId,
            imagesType.count,
            Formatter.formatFileSize(requireContext(), imagesType.overallSize)
        )
    }

    private fun displayBiggestFiles(statistics: Statistics) {
        binding.biggestFiles.adapter = BigFileAdapter(requireContext()).apply {
            addAll(statistics.biggestFiles)
        }
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}
