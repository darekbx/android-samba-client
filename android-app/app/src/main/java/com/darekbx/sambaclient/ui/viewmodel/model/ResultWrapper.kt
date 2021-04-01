package com.darekbx.sambaclient.ui.viewmodel.model

class ResultWrapper<T>(val result: T?, val exception: Exception? = null) {
    val hasError = exception != null

    constructor(exception: Exception) : this(null, exception)

    val errorMessage = exception?.message

    fun requireResult() =
        result ?: throw IllegalStateException("Result is null")
}
