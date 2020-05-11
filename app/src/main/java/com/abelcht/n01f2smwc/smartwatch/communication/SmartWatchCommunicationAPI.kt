package com.abelcht.n01f2smwc.smartwatch.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.smartwatch.communication.packages.ConfigurePackage
import java.util.*
import java.util.concurrent.CompletableFuture

class SmartWatchCommunicationAPI {
    // Tag for the logs
    private val logTag = "SmartWatchCommunicationAPI"

    // Is device connected
    private var isDeviceConnected = false

    // Connection callback
    var connectionFuture: CompletableFuture<Boolean>? = null

    // Bluetooth Gatt
    var bluetoothGatt: BluetoothGatt? = null

    // Bluetooth Gatt Characteristics
    var smartWatchWriteCharacteristic: BluetoothGattCharacteristic? = null
    var smartWatchNotificationCharacteristic: BluetoothGattCharacteristic? = null

    // Callback for bluetooth connection
    @ExperimentalUnsignedTypes
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // Log.i(TAG, "Connection change")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                this@SmartWatchCommunicationAPI.isDeviceConnected =
                    BluetoothGatt.STATE_CONNECTED == newState
//                Log.i(TAG, "Connection change effective, state $connectedDevice")
                if (this@SmartWatchCommunicationAPI.isDeviceConnected) {
                    Log.i(logTag, "Connected")
                    // Discover services
                    gatt!!.discoverServices()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            val smartWatchMainService =
                gatt!!.getService(UUID.fromString("c3e6fea0-e966-1000-8000-be99c223df6a"))
            if (smartWatchMainService != null) {
                smartWatchWriteCharacteristic =
                    smartWatchMainService.getCharacteristic(UUID.fromString("c3e6fea1-e966-1000-8000-be99c223df6a"))
                smartWatchNotificationCharacteristic =
                    smartWatchMainService.getCharacteristic(UUID.fromString("c3e6fea2-e966-1000-8000-be99c223df6a"))
                if (smartWatchWriteCharacteristic != null && smartWatchNotificationCharacteristic != null) {
                    Log.i(logTag, "Characteristics catch successfully")
                } else {
                    Log.i(logTag, "Error on characteristics catch")
                }

                // Send configure command
                configureSmartWatch()
            } else {
                Log.i(logTag, "Error on service catch")
            }
        }
    }

    @ExperimentalUnsignedTypes
    fun connectToSmartWatch(
        bluetoothDevice: BluetoothDevice,
        context: Context
    ): CompletableFuture<Boolean> {
        connectionFuture = CompletableFuture<Boolean>()
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback)
        return connectionFuture!!
    }

    @ExperimentalUnsignedTypes
    private fun configureSmartWatch(): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                ConfigurePackage(bluetoothGatt!!.device.address!!).getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    fun isConnectedToSmartWatch(): Boolean {
        // TODO: Improve
        return isDeviceConnected
    }

    fun disconnectFromSmartWatch() {
        isDeviceConnected = false
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
        }
    }

}