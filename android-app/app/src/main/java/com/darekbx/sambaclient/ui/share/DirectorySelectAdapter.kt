package com.darekbx.sambaclient.ui.share

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.AdapterSambaDirectoryBinding
import com.darekbx.sambaclient.ui.samba.SambaFile

class DirectorySelectAdapter(context: Context) :
    ArrayAdapter<SambaFile>(context, R.layout.adapter_samba_directory) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = when (convertView) {
            null -> DataBindingUtil.inflate(
                inflater,
                R.layout.adapter_samba_directory,
                parent,
                false
            )
            else -> DataBindingUtil.getBinding<AdapterSambaDirectoryBinding>(convertView)
        } as AdapterSambaDirectoryBinding

        return binding.apply { file = getItem(position) }.root
    }

    private val inflater by lazy { LayoutInflater.from(context) }
}
