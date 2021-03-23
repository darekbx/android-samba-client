package com.darekbx.sambaclient.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntEditTextPreference(context: Context, attrs: AttributeSet?) :
    EditTextPreference(context, attrs) {

    override fun getPersistedString(defaultReturnValue: String?): String {
        return "${getPersistedInt(defaultValue)}"
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(value?.toInt() ?: defaultValue)
    }

    private val defaultValue by lazy { -1 }
}
