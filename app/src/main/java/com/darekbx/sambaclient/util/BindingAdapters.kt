package com.darekbx.sambaclient.util

import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

@BindingAdapter("imageSource")
fun ImageView.setImage(imageResId: Int) {
    setImageResource(imageResId)
}

@BindingAdapter("dateTime")
fun TextView.setDateTime(timestamp: Long) {
    text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(timestamp)
}

@BindingAdapter("fileSize")
fun TextView.setFileSize(fileSize: Long) {
    text = Formatter.formatFileSize(context, fileSize)
}

@BindingAdapter("isVisible")
fun TextView.isVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}
