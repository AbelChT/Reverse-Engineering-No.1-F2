package com.abelcht.n01f2smwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.abelcht.n01f2smwc.smartwatch.communication.SmartWatchCommunicationAPI

class DisplayDataViewModel : ViewModel() {
    val smartWatchCommunicationAPI: SmartWatchCommunicationAPI = SmartWatchCommunicationAPI()
}
