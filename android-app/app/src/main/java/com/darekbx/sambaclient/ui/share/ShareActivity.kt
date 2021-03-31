package com.darekbx.sambaclient.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.ActivityShareBinding
import com.darekbx.sambaclient.preferences.AuthPreferences
import com.darekbx.sambaclient.ui.viewmodel.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ShareActivity : AppCompatActivity() {

    companion object {
        private const val UPLOAD_DELAY = 1000L
    }

    private val sambaViewModel: SambaViewModel by viewModel()
    private val authPreferences: AuthPreferences by inject()
    private val uriViewModel: UriViewModel by viewModel()

    private var _binding: ActivityShareBinding? = null
    private val binding get() = _binding!!

    private var selectedPath = ""
    private var fileNamesMap = mapOf<Uri, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sambaViewModel.autoAuthenticationResult.observe(this, { handleAuthenticationResult(it) })
        sambaViewModel.isLoading.observe(this, { showHideLoadingLayout(it) })
        uriViewModel.fileNames.observe(this) { handleFileNamesResult(it) }

        autoLogin()
        collectShareUris()

        binding.buttonCancel.setOnClickListener { finish() }
        binding.buttonShare.setOnClickListener { executeShare() }
        binding.documentLocation.setOnClickListener { openDirectorySelectDialog() }
    }

    private fun autoLogin() {
        val credentials = authPreferences.read()
        val shareName = authPreferences.readShareName()
        if (credentials.arePersisted && shareName != null) {
            with(credentials) {
                sambaViewModel.authenticate(address!!, user, password, shareName)
            }
        } else {
            displayError(getString(R.string.share_auth_error))
        }
    }

    private fun handleAuthenticationResult(resultWrapper: ResultWrapper<Boolean>) {
        if (resultWrapper.hasError) {
            displayError(resultWrapper.errorMessage ?: getString(R.string.share_common_error))
        } else {
            binding.buttonShare.isEnabled = true
        }
    }

    private fun handleFileNamesResult(resultWrapper: ResultWrapper<Map<Uri, String>>) {
        if (resultWrapper.hasError) {
            displayError(resultWrapper.errorMessage ?: getString(R.string.share_common_error))
        } else {
            fileNamesMap = resultWrapper.requireResult()
            displayFilesToShare(resultWrapper.requireResult().values.toList())
        }
    }

    private fun openDirectorySelectDialog() {
        val dialog = DirectorySelectDialog()
        dialog.onSelect = { path ->
            if (path.isNotEmpty()) {
                selectedPath = path
                binding.documentLocation.text = path
            }
        }
        dialog.show(supportFragmentManager, DirectorySelectDialog::class.java.name)
    }

    private fun displayError(message: String) {
        binding.shareError.text = message
    }

    private fun collectShareUris() {
        val fileUris = mutableListOf<Uri>()
        when (intent.action) {
            Intent.ACTION_SEND -> {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)
                    ?.let { uri -> fileUris.add(uri) }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
                    ?.mapNotNull { it as? Uri }
                    ?.let { uriList -> fileUris.addAll(uriList) }
            }
        }
        uriViewModel.retrieveFileNames(fileUris)
    }

    private fun displayFilesToShare(names: List<String>) {
        binding.documentTitle.text = names.joinToString(", ")
    }

    private fun executeShare() {
        val uploadDialog = UploadDialog()
        uploadDialog.onDismissed = { finish() }
        uploadDialog.showNow(supportFragmentManager, UploadDialog::class.java.simpleName)

        val filesToUpload = fileNamesMap.map { FileToUpload(it.key, it.value) }
        uploadDialog.addStates(filesToUpload)

        sambaViewModel.fileUploadState.observe(this, { uploadDialog.updateState(it) })
        sambaViewModel.fileUploadCompleted.observe(this, { uploadDialog.notifyUploadCompleted() })

        CoroutineScope(Dispatchers.IO).launch {
            delay(UPLOAD_DELAY)
            withContext(Dispatchers.Main) {
                sambaViewModel.uploadFiles(selectedPath, filesToUpload)
            }
        }
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}
