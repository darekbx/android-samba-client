package com.darekbx.sambaclient.ui.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darekbx.sambaclient.databinding.AdapterUploadStateBinding
import com.darekbx.sambaclient.viewmodel.model.FileUploadState

class UploadStateAdapter :
    RecyclerView.Adapter<UploadStateAdapter.BindingViewHolder>() {

    class BindingViewHolder(val viewBinding: AdapterUploadStateBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    private var data = listOf<FileUploadState>()

    fun fillData(data: List<FileUploadState>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun update(fileUploadState: FileUploadState) {
        val itemIndex = data.indexOfFirst { it.equalsByUri(fileUploadState) }

        data[itemIndex].exception = fileUploadState.exception
        data[itemIndex].uploaded = fileUploadState.uploaded

        notifyItemChanged(itemIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterUploadStateBinding.inflate(inflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        holder.viewBinding.uploadState = data[position]
    }

    override fun getItemCount() = data.size
}
