package com.abelcht.n01f2smwc.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.abelcht.n01f2smwc.R
import com.abelcht.n01f2smwc.openwheatherapi.getTemperaturePressure
import com.abelcht.n01f2smwc.openwheatherapi.getUV
import com.abelcht.n01f2smwc.ui.viewmodel.DisplayDataViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_data_display.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


class DisplayDataFragment : Fragment() {
    private val viewModel: DisplayDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    val SELECT_DEVICE_REQUEST_CODE = 43
    val REQUEST_ENABLE_BT = 41
    private val TAG = "DisplayDataFragment"

    private val deviceManager: CompanionDeviceManager by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getSystemService(CompanionDeviceManager::class.java)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart")
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.i(TAG, "onActivityCreated")

        // Location
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        // TODO: Add callback to bluetooth disconnect

        // To skip filtering based on name and supported feature flags (UUIDs),
        // don't include calls to setNamePattern() and addServiceUuid(),
        // respectively. This example uses Bluetooth.
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .build()

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of device names is presented to the user as
        // pairing options.
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(false)
            .build()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        connectionButton.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }

            // Check if location is enabled
            if (bluetoothAdapter?.isEnabled == true) {

                if (!viewModel.smartWatchCommunicationAPI.isConnectedToSmartWatch()) {
                    // When the app tries to pair with the Bluetooth device, show the
                    // appropriate pairing request dialog to the user.
                    deviceManager.associate(
                        pairingRequest,
                        object : CompanionDeviceManager.Callback() {

                            override fun onDeviceFound(chooserLauncher: IntentSender) {
                                startIntentSenderForResult(
                                    chooserLauncher,
                                    SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0, null
                                )
                            }

                            override fun onFailure(error: CharSequence?) {
                                // Handle failure
                            }
                        }, null
                    )

                    // Disable button
                    connectionButton.isEnabled = false
                } else {
                    viewModel.smartWatchCommunicationAPI.disconnectFromSmartWatch()
                    onSmartWatchDisconnected()
                }
            }
        }

        // Set buttons state
        if (viewModel.smartWatchCommunicationAPI.isConnectedToSmartWatch())
            onSmartWatchConnected()
        else
            onSmartWatchDisconnected()

        // Find button action
        findButton.setOnClickListener {
            val notificationResult = viewModel.smartWatchCommunicationAPI.sendSearchNotification()
            Log.i(TAG, "Find button pushed, result: $notificationResult")

        }

        // Obtain short-time constant parameters
        getDeviceLocation {
            if (it != null) {
                Log.i(
                    TAG,
                    "Device location is altitude ${it.altitude} latitude ${it.latitude}  longitude ${it.longitude}"
                )
                viewModel.altitude = it.altitude
                viewModel.latitude = it.latitude
                viewModel.longitude = it.longitude

                this.requireActivity().runOnUiThread {
                    altitudeTextView.text = "%.2f".format(viewModel.altitude)
                }

                getTemperaturePressure(
                    it.latitude, it.longitude, this.requireContext()
                ) { temperaturePressure ->
                    if (temperaturePressure != null) {
                        viewModel.pressure = temperaturePressure.second
                        viewModel.temperature = temperaturePressure.first - 273.16 // Convert to ºC

                        this.requireActivity().runOnUiThread {
                            temperatureTextView.text = "%.2f".format(viewModel.temperature)
                            pressureTextView.text = "%.2f".format(viewModel.pressure)
                        }
                    } else
                        Log.i(TAG, "temperaturePressure is null")
                }

                getUV(
                    it.latitude, it.longitude, this.requireContext()
                ) { uv ->
                    if (uv != null) {
                        viewModel.uv = uv
                        this.requireActivity().runOnUiThread {
                            uvTextView.text = "%.2f".format(viewModel.uv)
                        }
                    }
                }
            } else
                Log.i(TAG, "Device location es null")
        }

        // On date time change
        viewModel.onDateTimeChangeCallback = {
            val currentDateTime = LocalDateTime.now()
            // Change date and time
            val notificationResult =
                viewModel.smartWatchCommunicationAPI.changeDateTime(currentDateTime)

            Log.i(TAG, "Changed time, result: $notificationResult")
        }

        // On new call
        viewModel.onCallArriveCallback = {
            // Change date and time
            val notificationResult = viewModel.smartWatchCommunicationAPI.sendCallNotification()

            Log.i(TAG, "New call, result: $notificationResult")
        }

        // On new message
        viewModel.onMessageArriveCallback = {
            // Change date and time
            val notificationResult = viewModel.smartWatchCommunicationAPI.sendMessageNotification()

            Log.i(TAG, "New message, result: $notificationResult")
        }

        viewModel.smartWatchCommunicationAPI.changePedometerListener {
            requireActivity().runOnUiThread {
                stepsTextView.text = it.toString()
            }

            Log.i(TAG, "Modified steps textView")
        }
    }

    /**
     * Obtain the location of the device
     */
    private fun getDeviceLocation(callBackOnLocationObtained: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Log.i(TAG, "Obtained location")
                    callBackOnLocationObtained(location)
                }
        } else {
            Log.i(TAG, "No permission granted")
            callBackOnLocationObtained(null)
        }
    }

    /**
     * Actions executed when the smart watch connect to the bluetooth
     */
    fun onSmartWatchConnected() {
        // Change button text and state
        connectionButton.isEnabled = true
        connectionButton.text = getString(R.string.disconnect)
        findButton.isEnabled = true

        // Change date and time
        val currentDateTime = LocalDateTime.now()

        val notificationResult =
            viewModel.smartWatchCommunicationAPI.changeDateTime(currentDateTime)

        Log.i(TAG, "Changed time, result: $notificationResult")

        // If two communications with the Smartwatch are done in a short space of time, it fails in the
        // last one
        Thread.sleep(100)

        // Change uv pressure temperature and altitude
        val changeUVPressureTemperatureAltitudeResult =
            viewModel.smartWatchCommunicationAPI.changeAltitudeTemperaturePressureUV(
                viewModel.altitude,
                viewModel.temperature,
                viewModel.pressure,
                viewModel.uv
            )

        Log.i(
            TAG,
            "Changed changeUVPressureTemperatureAltitudeResult, result: $changeUVPressureTemperatureAltitudeResult"
        )

        // If two communications with the Smartwatch are done in a short space of time, it fails in the
        // last one
        Thread.sleep(100)

        // Change alarm
        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock
        if (nextAlarm != null) {
            val nextAlarmDate = Date(nextAlarm.triggerTime)
            Log.i(TAG, "Next alarm in: ${nextAlarmDate.hours}:${nextAlarmDate.minutes}")

            val changeAlarmResult = viewModel.smartWatchCommunicationAPI.changeAlarm(
                LocalTime.of(nextAlarmDate.hours, nextAlarmDate.minutes)
            )

            Log.i(TAG, "Changed changeAlarmResult, result: $changeAlarmResult")
        } else {
            Log.i(TAG, "No next alarm")
            val changeAlarmResult = viewModel.smartWatchCommunicationAPI.changeAlarm(null)

            Log.i(TAG, "Changed changeAlarmResult, result: $changeAlarmResult")
        }
    }

    /**
     * Actions executed when the smart watch connect to the bluetooth fails
     */
    fun onSmartWatchFailConnection() {
        // Enable button
        connectionButton.isEnabled = true

        // Show toast
        val text = "Failed to connect"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(this.context, text, duration)
        toast.show()
    }

    /**
     * Actions executed when the smart watch disconnect from the bluetooth
     */
    fun onSmartWatchDisconnected() {
        // Change button signature
        connectionButton.text = getString(R.string.connect)
        findButton.isEnabled = false

        viewModel.smartWatchCommunicationAPI.changePedometerListener(null)

        // Delete callbacks
//        if (viewModel.smartWatchCommunicationAPI.isConnectedToSmartWatch()) {
//            requireActivity().unregisterReceiver(timeAndDateChangeBroadcastReceiver)
//        }

        // TODO: Delete callbacks
    }

    @ExperimentalUnsignedTypes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data != null) {
                        val deviceToPair: Any? =
                            data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                        if (deviceToPair is BluetoothDevice) {
                            val deviceAddress = deviceToPair.address
                            Log.i(TAG, "Device address: $deviceAddress")

                            viewModel.smartWatchCommunicationAPI.connectToSmartWatch(
                                deviceToPair,
                                this.requireContext()
                            ) {
                                Log.i(TAG, "Updating UI")
                                if (it) {
                                    activity?.runOnUiThread { onSmartWatchConnected() }
                                } else {
                                    activity?.runOnUiThread { onSmartWatchFailConnection() }
                                }
                            }

                        } else println("Unrecognised type of device")
                    }
                }
            }
        }
    }
}
