package com.darekbx.sambaclient.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import com.darekbx.sambaclient.ui.remotecontrol.RemoteControl
import com.darekbx.sambaclient.ui.remotecontrol.Statistics

class RemoteControlViewModel(
    private val remoteControl: RemoteControl
) : LoadingViewModel() {

    val statisticsResult = MutableLiveData<ResultWrapper<Statistics>>()

    fun retrieveStatistics(
        maintenanceServerAddress: String,
        md5Credentials: String
    ) {
        runIOInViewModelScope {
            try {
                val statistics = remoteControl.retrieveStatistics(
                    maintenanceServerAddress, md5Credentials)
                statisticsResult.postValue(ResultWrapper(statistics))
            } catch (e: Exception) {
                e.printStackTrace()
                statisticsResult.postValue(ResultWrapper(e))
            }
        }
    }
}
