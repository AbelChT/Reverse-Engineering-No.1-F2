package com.abelcht.n01f2smwc.ui.fragment

import android.app.Activity
import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.abelcht.n01f2smwc.ui.viewmodel.DisplayDataViewModel
import com.abelcht.n01f2smwc.R
import kotlinx.android.synthetic.main.display_data_fragment.*
import java.util.*


class DisplayDataFragment : Fragment() {
    private val viewModel: DisplayDataViewModel by activityViewModels()

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Add callback to bluetooth disconnect

        // TODO: Request bluetooth and location

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
