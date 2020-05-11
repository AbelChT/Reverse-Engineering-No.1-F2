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
    var connectedDevice = false
    var bluetoothGatt: BluetoothGatt? = null
    var smartWatchWriteCharacteristicGlobal: BluetoothGattCharacteristic? = null

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // Log.i(TAG, "Connection change")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectedDevice = BluetoothGatt.STATE_CONNECTED == newState
//                Log.i(TAG, "Connection change effective, state $connectedDevice")
                if (connectedDevice) {
                    Log.i(TAG, "Connected")
                    // Discover services
                    gatt!!.discoverServices()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
//            Log.i(TAG, "Service discovered")
//            gatt!!.services.forEach({
//                Log.i(TAG, "Service UUID ${it.uuid}")
//                it.characteristics.forEach {
//                    Log.i(TAG, "\t Characteristic UUID ${it.uuid}")
//                    Log.i(TAG, "\t\t Permissions ${it.permissions}")
//                    Log.i(TAG, "\t\t Properties ${it.properties}")
//                }
//            })
            val smartWatchMainService =
                gatt!!.getService(UUID.fromString("c3e6fea0-e966-1000-8000-be99c223df6a"))
            if (smartWatchMainService != null) {
                // Characteristic write c3e6fea1-e966-1000-8000-be99c223df6a
                // Characteristic notification c3e6fea2-e966-1000-8000-be99c223df6a
                val smartWatchWriteCharacteristic =
                    smartWatchMainService.getCharacteristic(UUID.fromString("c3e6fea1-e966-1000-8000-be99c223df6a"))
                val smartWatchNotificationCharacteristic =
                    smartWatchMainService.getCharacteristic(UUID.fromString("c3e6fea2-e966-1000-8000-be99c223df6a"))
                if (smartWatchWriteCharacteristic != null && smartWatchNotificationCharacteristic != null) {
                    Log.i(TAG, "Characteristics catch successfully")
                    smartWatchWriteCharacteristicGlobal = smartWatchWriteCharacteristic
                    smartWatchWriteCharacteristicGlobal!!.value = byteArrayOf(
                        0xa9.toByte(),
                        0x32.toByte(),
                        0x00.toByte(),
                        0x0c.toByte(),
                        0x40.toByte(),
                        0xe2.toByte(),
                        0x01.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0xa9.toByte(),
                        0xbc.toByte(),
                        0x7a.toByte(),
                        0x8e.toByte(),
                        0xf0.toByte(),
                        0xad.toByte(),
                        0x14.toByte()
                    )

                    var characteristicWriteReturn =
                        bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristicGlobal)

                    Log.i(TAG, "Sent first value $characteristicWriteReturn")
                } else {
                    Log.i(TAG, "Error on characteristics catch")
                }
            } else {
                Log.i(TAG, "Error on service catch")
            }
        }
    }

    private val deviceManager: CompanionDeviceManager by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getSystemService(CompanionDeviceManager::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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


        connectionButton.setOnClickListener {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }

            // Check if location is enabled
            if (bluetoothAdapter?.isEnabled == true) {
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
            }

        }

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

                            bluetoothGatt =
                                deviceToPair.connectGatt(this.context, false, bluetoothGattCallback)
                        } else println("Unrecognised type of device")
                    }
                    // User has chosen to pair with the Bluetooth device.
//                    val deviceToPair: BluetoothDevice =
//                            data!!.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
//                    deviceToPair.createBond()
//                    // ... Continue interacting with the paired device.

                }
            }
        }
    }
}
