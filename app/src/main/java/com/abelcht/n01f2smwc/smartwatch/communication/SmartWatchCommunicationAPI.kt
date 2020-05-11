package com.abelcht.n01f2smwc.smartwatch.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.smartwatch.communication.packages.ConfigurePackage
import com.abelcht.n01f2smwc.smartwatch.communication.packages.SearchNotificationPackage
import java.util.*

class SmartWatchCommunicationAPI {
    // Tag for the logs
    private val logTag = "SmartWatchCommunicationAPI"

    // Is device connected
    private var isDeviceConnected = false

    // Connection callback
    private var onConnectionCompleteCallback: ((Boolean) -> Unit)? = null

    // Bluetooth Gatt
    private var bluetoothGatt: BluetoothGatt? = null

    // Bluetooth Gatt Characteristics
    private var smartWatchWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var smartWatchNotificationCharacteristic: BluetoothGattCharacteristic? = null

    // Callback for bluetooth connection
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
                    onConnectionCompleteCallback?.invoke(true)
                } else {
                    Log.i(logTag, "Error on characteristics catch")
                    onConnectionCompleteCallback?.invoke(false)
                }

                // Send configure command
                configureSmartWatch()
            } else {
                Log.i(logTag, "Error on service catch")
                onConnectionCompleteCallback?.invoke(false)
            }
        }
    }

    /**
     * Connect to the SmartWatch
     */
    fun connectToSmartWatch(
        bluetoothDevice: BluetoothDevice,
        context: Context,
        onCompleteCallback: (Boolean) -> Unit
    ) {
        onConnectionCompleteCallback = onCompleteCallback
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback)
    }

    /**
     * Configure SmartWatch
     */
    private fun configureSmartWatch(): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                ConfigurePackage(bluetoothGatt!!.device.address!!).getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    /**
     * Return true if SmartWatch is connected
     */
    fun isConnectedToSmartWatch(): Boolean {
        return isDeviceConnected
    }

    /**
     * Disconnect from SmartWatch
     */
    fun disconnectFromSmartWatch() {
        isDeviceConnected = false
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
        }
    }

    /**
     * Send search notification
     */
    fun sendSearchNotification(): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                SearchNotificationPackage().getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

}