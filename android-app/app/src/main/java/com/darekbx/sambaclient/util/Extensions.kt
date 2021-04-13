package com.darekbx.sambaclient.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import java.math.BigInteger
import java.security.MessageDigest

fun String.toMd5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun <T> Fragment.observeOnViewLifecycle(liveData: LiveData<T>, body: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner, body)
}
