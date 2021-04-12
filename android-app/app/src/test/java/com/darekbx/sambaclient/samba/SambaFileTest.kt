package com.darekbx.sambaclient.samba

import com.darekbx.sambaclient.R
import com.hierynomus.msfscc.FileAttributes
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SambaFileTest {

    @Test
    fun testIsDirectory() {
        val attributes = FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value
        assertTrue(SambaFile("name", 0L, 0L, 0L, attributes).isDirectory)
    }

    @Test
    fun testIsUpMark() {
        assertTrue(SambaFile(".", 0L, 0L, 0L, 0L).isUpMark)
        assertTrue(SambaFile("..", 0L, 0L, 0L, 0L).isUpMark)
        assertFalse(SambaFile("", 0L, 0L, 0L, 0L).isUpMark)
    }

    @Test
    fun testGetIcon() {
        assertEquals(R.drawable.ic_file_image, SambaFile("name.jpg", 0L, 0L, 0L, 0L).icon)
        assertEquals(R.drawable.ic_text_file, SambaFile("name.doc", 0L, 0L, 0L, 0L).icon)
        assertEquals(R.drawable.ic_archive_file, SambaFile("name.zip", 0L, 0L, 0L, 0L).icon)
        assertEquals(R.drawable.ic_common_file, SambaFile("name.log", 0L, 0L, 0L, 0L).icon)
    }
}
