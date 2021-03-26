package com.darekbx.sambaclient.ui.maintenance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.databinding.AdapterBigFileBinding
import com.darekbx.sambaclient.ui.remotecontrol.File

class BigFileAdapter(context: Context) : ArrayAdapter<File>(context, R.layout.adapter_big_file) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = when (convertView) {
            null -> DataBindingUtil.inflate(inflater, R.layout.adapter_big_file, parent, false)
            else -> DataBindingUtil.getBinding<AdapterBigFileBinding>(convertView)
        } as AdapterBigFileBinding

        return binding.apply {
            file = getItem(position)
        }.root
    }

    private val inflater by lazy { LayoutInflater.from(context) }
}
