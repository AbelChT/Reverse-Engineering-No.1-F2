package com.abelcht.n01f2smwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.abelcht.n01f2smwc.smartwatch.communication.SmartWatchCommunicationAPI

class DisplayDataViewModel : ViewModel() {
    // Communication API
    val smartWatchCommunicationAPI: SmartWatchCommunicationAPI = SmartWatchCommunicationAPI()

    // Short-time persistent parameters
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var altitude: Double = 0.0

    var uv: Double = 0.0
    var temperature: Double = 0.0
    var pressure: Double = 0.0

    // System event callbacks
    var onDateTimeChangeCallback: (() -> Unit)? = null

    var onCallArriveCallback: (() -> Unit)? = null

    var onMessageArriveCallback: (() -> Unit)? = null
}
