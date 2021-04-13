package com.darekbx.sambaclient.ui.explorer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.FragmentFileBinding
import com.darekbx.sambaclient.samba.SambaClientWrapper
import com.darekbx.sambaclient.samba.SambaFile
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import com.darekbx.sambaclient.util.observeOnViewLifecycle
import com.darekbx.sambaclient.util.setDateTime
import com.darekbx.sambaclient.util.setFileSize
import com.darekbx.sambaclient.util.setImage
import com.darekbx.sambaclient.viewmodel.BaseAccessViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class FileFragment : Fragment(R.layout.fragment_file) {

    private val accessViewModel: BaseAccessViewModel by viewModel()

    private var _binding: FragmentFileBinding? = null
    private val binding get() = _binding!!

    private var downloadedFile: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeOnViewLifecycle(accessViewModel.isLoading) { showHideLoadingLayout(it) }
        observeOnViewLifecycle(accessViewModel.fileInfoResult) { handleFileInfoResult(it) }
        observeOnViewLifecycle(accessViewModel.fileDownloadResult) { handleFileDownloadResult(it) }
        observeOnViewLifecycle(accessViewModel.fileDeleteResult) { handleFileDeleteResult(it) }

        getFilePathArgument()?.let { filePath ->
            accessViewModel.fileInfo(filePath)
        }

        handleEvents()
    }

    private fun handleEvents() {
        binding.downloadFile.setOnClickListener {
            getFilePathArgument()?.let { filePath ->
                if (downloadedFile != null) {
                    openFile(downloadedFile)
                } else {
                    accessViewModel.downloadFile(filePath)
                }
            }
        }
        binding.deleteFile.setOnClickListener {
            getFilePathArgument()?.let { filePath ->
                accessViewModel.deleteFile(filePath)
            }
        }
    }

    private fun openFile(downloadedFile: String?) {
        if (downloadedFile == null) return
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(downloadedFile)
        val mime = obtainMimeType(uri)
        intent.setDataAndType(uri, mime)
        startActivity(intent)
    }

    private fun obtainMimeType(uri: Uri): String {
        var mime: String? = null
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val mimeExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        if (mimeTypeMap.hasExtension(mimeExtension)) {
            mime = mimeTypeMap.getMimeTypeFromExtension(mimeExtension)
        }
        return mime ?: "*/*"
    }

    private fun getFilePathArgument() = arguments?.getString(FileExplorerFragment.FILE_PATH_KEY)

    private fun handleFileDownloadResult(resultWrapper: ResultWrapper<String>) {
        val message =
            if (resultWrapper.hasError) resultWrapper.errorMessage
            else "${getString(R.string.file_saved_to)} ${resultWrapper.result}"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        if (!resultWrapper.hasError) {
            binding.downloadFile.setText(R.string.file_open)
            downloadedFile = resultWrapper.result
        }
    }

    private fun handleFileDeleteResult(resultWrapper: ResultWrapper<Boolean>) {
        val message =
            if (resultWrapper.hasError) resultWrapper.errorMessage
            else getString(R.string.file_was_deleted)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        if (!resultWrapper.hasError) {
            // Go back after delete
            findNavController().popBackStack()
        }
    }

    private fun handleFileInfoResult(resultWrapper: ResultWrapper<SambaFile>) {
        if (resultWrapper.hasError) {
            Toast.makeText(requireContext(), resultWrapper.errorMessage, Toast.LENGTH_LONG).show()
        } else {
            displayFileInfo(resultWrapper.requireResult())
        }
    }

    private fun displayFileInfo(file: SambaFile) {
        binding.fileIcon.setImage(file.icon)
        binding.fileName.text = file.name.substringAfterLast(SambaClientWrapper.PATH_DELIMITER)
        binding.fileLocation.text = file.name.substringBeforeLast(SambaClientWrapper.PATH_DELIMITER)
        binding.fileDate.setDateTime(file.changeTime)
        binding.fileSize.setFileSize(file.size)
    }

    private fun showHideLoadingLayout(isLoading: Boolean) {
        binding.loadingLayout.loadingLayout.visibility = when (isLoading) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}
