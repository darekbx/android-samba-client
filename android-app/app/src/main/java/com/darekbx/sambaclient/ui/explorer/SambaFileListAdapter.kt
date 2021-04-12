package com.darekbx.sambaclient.ui.explorer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.darekbx.sambaclient.BR
import com.darekbx.sambaclient.samba.SambaFile

abstract class SambaFileListAdapter<T: ViewDataBinding> : BaseSambaFileAdapter() {

    interface SambaFileClickListener {
        fun onClick(file: SambaFile)
    }

    class BindingViewHolder(val viewBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    private val data = mutableListOf<SambaFile>()

    abstract fun inflateSambaFileView(inflater: LayoutInflater, root: ViewGroup): T

    override fun swapData(list: List<SambaFile>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = inflateSambaFileView(inflater, parent)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sambaFile = data[position]
        with((holder as BindingViewHolder).viewBinding) {
            setVariable(BR.file, sambaFile)
            setVariable(BR.sambaFileClickListener, onClickHolder)
            executePendingBindings()
        }
    }

    override fun getItemCount() = data.size

    private val onClickHolder = object : SambaFileClickListener {
        override fun onClick(file: SambaFile) {
            onSambaFileClick(file)
        }
    }
}
