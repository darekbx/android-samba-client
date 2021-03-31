package com.darekbx.sambaclient.ui.viewmodel

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.lang.Exception

class FileUploadStateTest {

    @Test
    fun testErrorImageInvisible() {
        assertTrue(FileUploadState(mock()).errorImageInvisible())
        assertTrue(FileUploadState(mock(), uploaded = false).errorImageInvisible())
        assertTrue(FileUploadState(mock(), uploaded = true).errorImageInvisible())

        assertFalse(FileUploadState(mock(), exception = Exception()).errorImageInvisible())
    }

    @Test
    fun testUploadedImageInvisible() {
        assertTrue(FileUploadState(mock()).uploadedImageInvisible())
        assertTrue(FileUploadState(mock(), uploaded = false).uploadedImageInvisible())

        assertFalse(FileUploadState(mock(), uploaded = true).uploadedImageInvisible())
    }

    @Test
    fun testProgressInvisible() {
        assertFalse(FileUploadState(mock()).progressInvisible())
        assertFalse(FileUploadState(mock(), uploaded = false).progressInvisible())

        assertTrue(FileUploadState(mock(), uploaded = true).progressInvisible())
        assertTrue(FileUploadState(mock(), exception = Exception()).progressInvisible())
    }
}
