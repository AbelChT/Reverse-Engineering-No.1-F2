package com.abelcht.n01f2smwc.ui.fragment

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.abelcht.n01f2smwc.R
import com.abelcht.n01f2smwc.ui.viewmodel.DisplayDataViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.display_data_fragment.*
import java.time.LocalDateTime


class DisplayDataFragment : Fragment() {
    private val viewModel: DisplayDataViewModel by activityViewModels()

    private val timeAndDateChangeBroadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
                    if (viewModel.smartWatchCommunicationAPI.isConnectedToSmartWatch()) {
                        val currentDateTime = LocalDateTime.now()
                        // Change date and time
                        val notificationResult =
                            viewModel.smartWatchCommunicationAPI.changeDateTime(currentDateTime)

                        Log.i(TAG, "Changed time, result: $notificationResult")
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.display_data_fragment, container, false)
    }

    val SELECT_DEVICE_REQUEST_CODE = 43
    val REQUEST_ENABLE_BT = 41
    private val TAG = "MainActivity"

    private val deviceManager: CompanionDeviceManager by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getSystemService(CompanionDeviceManager::class.java)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Location
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        // TODO: Add callback to bluetooth disconnect

        // TODO: Request bluetooth and location

        // TODO: Request bluetooth and location permissions if not granted


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

        alarmButton.setOnClickListener {
            // TODO: Implement, this is only a test
            // Change UV ...

            getDeviceLocation {
                if (it != null) {
                    Log.i(
                        TAG,
                        "Device location es altitude ${it.altitude} latitude ${it.latitude}  longitude ${it.longitude}"
                    )
                } else
                    Log.i(TAG, "Device location es null")
            }

//            val currentDateTime = LocalDateTime.now()
//
//            val notificationResult =
//                viewModel.smartWatchCommunicationAPI.changeDateTime(currentDateTime)
//
//            Log.i(TAG, "Changed time, result: $notificationResult")
        }
    }

    /**
     * Obtain the location of the device
     */
    fun getDeviceLocation(callBackOnLocationObtained: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callBackOnLocationObtained(location)
                }
        } else {
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

        alarmButton.isEnabled = true
        actionButton.isEnabled = true
        findButton.isEnabled = true

        // Change date and time
        val currentDateTime = LocalDateTime.now()

        val notificationResult =
            viewModel.smartWatchCommunicationAPI.changeDateTime(currentDateTime)

        Log.i(TAG, "Changed time, result: $notificationResult")

        val changeDateTimeIntentFilter = IntentFilter()
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIME_TICK)
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED)

        // TODO: Change
//        requireActivity().registerReceiver(
//            timeAndDateChangeBroadcastReceiver,
//            changeDateTimeIntentFilter
//        )

        // TODO: Add callback to calls and messages

        // TODO: Change UV, Temperature ...
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
        alarmButton.isEnabled = false
        actionButton.isEnabled = false
        findButton.isEnabled = false

        // Delete callbacks
        if (viewModel.smartWatchCommunicationAPI.isConnectedToSmartWatch()) {
            requireActivity().unregisterReceiver(timeAndDateChangeBroadcastReceiver)
        }

        // TODO: Delete callbacks
    }

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
