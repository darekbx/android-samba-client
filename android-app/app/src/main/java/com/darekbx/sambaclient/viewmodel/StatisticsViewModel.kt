package com.darekbx.sambaclient.viewmodel

import androidx.lifecycle.MutableLiveData
import com.darekbx.sambaclient.statistics.RemoteStatistics
import com.darekbx.sambaclient.statistics.Statistics
import com.darekbx.sambaclient.statistics.SubDirStatistics
import com.darekbx.sambaclient.viewmodel.model.ResultWrapper

class StatisticsViewModel(
    private val remoteStatistics: RemoteStatistics
) : LoadingViewModel() {

    val statisticsResult = MutableLiveData<ResultWrapper<Statistics>>()
    val subDirStatisticsResult = MutableLiveData<ResultWrapper<SubDirStatistics>>()

    fun retrieveStatistics(
        maintenanceServerAddress: String,
        md5Credentials: String
    ) {
        runIOInViewModelScope {
            try {
                val statistics = remoteStatistics.retrieveStatistics(
                    maintenanceServerAddress, md5Credentials)
                statisticsResult.postValue(ResultWrapper(statistics))
            } catch (e: Exception) {
                e.printStackTrace()
                statisticsResult.postValue(ResultWrapper(e))
            }
        }
    }

    fun retrieveStatistics(
        maintenanceServerAddress: String,
        md5Credentials: String,
        subDir: String
    ) {
        runIOInViewModelScope {
            try {
                val statistics = remoteStatistics.retrieveStatistics(
                    maintenanceServerAddress, md5Credentials, subDir)
                subDirStatisticsResult.postValue(ResultWrapper(statistics))
            } catch (e: Exception) {
                e.printStackTrace()
                subDirStatisticsResult.postValue(ResultWrapper(e))
            }
        }
    }
}
