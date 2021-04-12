package com.darekbx.sambaclient.ui.explorer

import androidx.recyclerview.widget.RecyclerView
import com.darekbx.sambaclient.samba.SambaFile

abstract class BaseSambaFileAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onSambaFileClick: (SambaFile) -> Unit = { }

    abstract fun swapData(list: List<SambaFile>)
}
