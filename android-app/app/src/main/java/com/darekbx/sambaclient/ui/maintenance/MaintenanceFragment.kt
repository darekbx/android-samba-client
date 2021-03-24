package com.darekbx.sambaclient.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentMaintenanceBinding
import com.darekbx.sambaclient.ui.remotecontrol.Statistics
import com.darekbx.sambaclient.ui.samba.Credentials
import com.darekbx.sambaclient.ui.viewmodel.RemoteControlViewModel
import com.darekbx.sambaclient.ui.viewmodel.ResultWrapper
import com.darekbx.sambaclient.ui.viewmodel.SambaViewModel
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * RPi maintentance server:
 * - created with http server
 * - communication through json
 * - authorization: md5("${samba.auth.context.user}_${samba.auth.context.password}")
 *
 * - used space
 * - free space
 * - last backup date
 * - make backup (select usb drive for a backup, detect usb drives)
 * - 10 biggest files
 * - chart by file sizes and file types?
 */
class MaintenanceFragment: Fragment(R.layout.fragment_maintenance) {

    private val sambaViewModel: SambaViewModel by viewModel()
    private val remoteControlViewModel: RemoteControlViewModel by viewModel()

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

        with(remoteControlViewModel) {
            observeOnViewLifecycle(isLoading) { showHideLoadingLayout(it) }
            observeOnViewLifecycle(statisticsResult) { handleStatistics(it) }
        }
    }

    private fun handleMd5Credentials(resultWrapper: ResultWrapper<Credentials>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {
            val credentials = resultWrapper.requireResult()
            remoteControlViewModel.retrieveStatistics(credentials.hostname, credentials.md5Credentials)
        }
    }

    private fun handleStatistics(resultWrapper: ResultWrapper<Statistics>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {

            // TODO display statistics

        }
    }


    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}
