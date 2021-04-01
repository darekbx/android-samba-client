package com.darekbx.sambaclient.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.darekbx.sambaclient.TestCoroutineRule
import com.darekbx.sambaclient.ui.statistics.RemoteStatistics
import com.darekbx.sambaclient.ui.statistics.Statistics
import com.darekbx.sambaclient.ui.statistics.SubDirStatistics
import com.darekbx.sambaclient.ui.viewmodel.model.ResultWrapper
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StatisticsViewModelTest : TestCase() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var statisticsObserver: Observer<ResultWrapper<Statistics>>

    @Mock
    private lateinit var subDirStatisticsObserver: Observer<ResultWrapper<SubDirStatistics>>

    @Test
    fun `Retrieve statistics`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val statistics = mock<Statistics>()
            val remoteStatistics = mock<RemoteStatistics>()
            doReturn(statistics).whenever(remoteStatistics).retrieveStatistics(any(), any())

            val viewModel = StatisticsViewModel(remoteStatistics)

            // When
            viewModel.statisticsResult.observeForever(statisticsObserver)
            viewModel.retrieveStatistics("address", "token")

            // Then
            val captor = argumentCaptor<ResultWrapper<Statistics>>()
            verify(statisticsObserver, times(1)).onChanged(captor.capture())

            assertEquals(statistics, captor.firstValue.requireResult())
        }
    }

    @Test
    fun `Retrieve statistics error`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val remoteStatistics = mock<RemoteStatistics>()
            doThrow(RuntimeException()).whenever(remoteStatistics).retrieveStatistics(any(), any())

            val viewModel = StatisticsViewModel(remoteStatistics)

            // When
            viewModel.statisticsResult.observeForever(statisticsObserver)
            viewModel.retrieveStatistics("address", "token")

            // Then
            val captor = argumentCaptor<ResultWrapper<Statistics>>()
            verify(statisticsObserver, times(1)).onChanged(captor.capture())

            assertNull(captor.firstValue.result)
            assertTrue(captor.firstValue.exception is RuntimeException)
        }
    }

    @Test
    fun `Retrieve sub dir statistics`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val subDirStatistics = mock<SubDirStatistics>()
            val remoteStatistics = mock<RemoteStatistics>()
            doReturn(subDirStatistics).whenever(remoteStatistics).retrieveStatistics(eq("address"), eq("token"), eq("dir"))

            val viewModel = StatisticsViewModel(remoteStatistics)

            // When
            viewModel.subDirStatisticsResult.observeForever(subDirStatisticsObserver)
            viewModel.retrieveStatistics("address", "token", "dir")

            // Then
            val captor = argumentCaptor<ResultWrapper<SubDirStatistics>>()
            verify(subDirStatisticsObserver, times(1)).onChanged(captor.capture())

            assertEquals(subDirStatistics, captor.firstValue.requireResult())
        }
    }

    @Test
    fun `Retrieve sub dir statistics exception`() {
        testCoroutineRule.runBlockingTest {
            // Given
            val remoteStatistics = mock<RemoteStatistics>()
            doThrow(RuntimeException()).whenever(remoteStatistics).retrieveStatistics(eq("address"), eq("token"), eq("dir"))

            val viewModel = StatisticsViewModel(remoteStatistics)

            // When
            viewModel.subDirStatisticsResult.observeForever(subDirStatisticsObserver)
            viewModel.retrieveStatistics("address", "token", "dir")

            // Then
            val captor = argumentCaptor<ResultWrapper<SubDirStatistics>>()
            verify(subDirStatisticsObserver, times(1)).onChanged(captor.capture())

            assertNull(captor.firstValue.result)
            assertTrue(captor.firstValue.exception is RuntimeException)
        }
    }
}
