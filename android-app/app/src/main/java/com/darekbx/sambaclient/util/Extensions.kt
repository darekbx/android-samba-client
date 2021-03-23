package com.darekbx.sambaclient.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun <T> Fragment.observeOnViewLifecycle(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner, body)
}
