package com.darekbx.sambaclient.ui.explorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darekbx.sambaclient.databinding.AdapterSambaDirectoryBinding
import com.darekbx.sambaclient.databinding.AdapterSambaFileBinding
import com.darekbx.sambaclient.ui.samba.SambaFile

class SambaFileAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DIR = 0
        private const val TYPE_FILE = 1
    }

    class FileViewHolder(val viewBinding: AdapterSambaFileBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    class DirectoryViewHolder(val viewBinding: AdapterSambaDirectoryBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    var onFileClick: (SambaFile) -> Unit = { }
    var onDirectoryClick: (SambaFile) -> Unit = { }

    private val data = mutableListOf<SambaFile>()

    fun swapData(list: List<SambaFile>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isDirectory) TYPE_DIR else TYPE_FILE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DIR -> {
                val binding = AdapterSambaDirectoryBinding.inflate(inflater, parent, false)
                binding.root.setOnClickListener { onDirectoryClick(binding.directory) }
                DirectoryViewHolder(binding)
            }
            else -> {
                val binding = AdapterSambaFileBinding.inflate(inflater, parent, false)
                binding.root.setOnClickListener { onFileClick(binding.file) }
                FileViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sambaFile = data[position]
        when (holder.itemViewType) {
            TYPE_DIR -> (holder as DirectoryViewHolder).viewBinding.directory = sambaFile
            else -> (holder as FileViewHolder).viewBinding.file = sambaFile
        }
    }

    override fun getItemCount() = data.size
}
