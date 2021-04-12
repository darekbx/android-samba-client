package com.darekbx.sambaclient.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.darekbx.sambaclient.TestCoroutineRule
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper
import com.darekbx.sambaclient.util.UriUtils
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UriViewModelTest: TestCase() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var observer: Observer<ResultWrapper<Map<Uri, String>>>

    @Test
    fun `Retrieve file names`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val uri1 = mock<Uri>()
            val uri2 = mock<Uri>()
            val uriUtils = mock<UriUtils> {
                on { getUriFileName(eq(uri1)) } doReturn "test_file.jpg"
                on { getUriFileName(eq(uri2)) } doReturn "test_file.doc"
            }
            val uriViewModel = UriViewModel(uriUtils)

            // When
            uriViewModel.fileNames.observeForever(observer)
            uriViewModel.retrieveFileNames(listOf(uri1, uri2))

            // Then
            val captor = argumentCaptor<ResultWrapper<Map<Uri, String>>>()
            verify(observer, times(1)).onChanged(captor.capture())

            assertEquals("test_file.jpg", captor.firstValue.requireResult().get(uri1))
            assertEquals("test_file.doc", captor.firstValue.requireResult().get(uri2))
        }
    }

    @Test
    fun `Retrieve file names exception`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val uri1 = mock<Uri>()
            val uriUtils = mock<UriUtils> {
                on { getUriFileName(eq(uri1)) } doThrow RuntimeException()
            }
            val uriViewModel = UriViewModel(uriUtils)

            // When
            uriViewModel.fileNames.observeForever(observer)
            uriViewModel.retrieveFileNames(listOf(uri1))

            // Then
            val captor = argumentCaptor<ResultWrapper<Map<Uri, String>>>()
            verify(observer, times(1)).onChanged(captor.capture())

            assertNull(captor.firstValue.result)
            assertTrue(captor.firstValue.exception is java.lang.RuntimeException)
        }
    }
}
