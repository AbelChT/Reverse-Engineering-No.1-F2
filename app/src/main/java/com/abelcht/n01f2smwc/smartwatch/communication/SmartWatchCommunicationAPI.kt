package com.abelcht.n01f2smwc.smartwatch.communication

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.smartwatch.communication.packages.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.math.roundToInt


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

                    // Activate notifications
                    bluetoothGatt!!.setCharacteristicNotification(
                        smartWatchNotificationCharacteristic, true
                    )

                    // Send configure command
                    configureSmartWatch()

                    onConnectionCompleteCallback?.invoke(true)
                } else {
                    Log.i(logTag, "Error on characteristics catch")
                    onConnectionCompleteCallback?.invoke(false)
                }
            } else {
                Log.i(logTag, "Error on service catch")
                onConnectionCompleteCallback?.invoke(false)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            // TODO: Complete it
            Log.i(
                logTag,
                "On characteristic change ${characteristic!!.uuid} ${characteristic.value}"
            )
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

    /**
     * Change date and time
     */
    fun changeDateTime(localDateTime: LocalDateTime): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                ChangeDateTimePackage(
                    localDateTime.hour,
                    localDateTime.minute,
                    localDateTime.second,
                    localDateTime.dayOfMonth,
                    localDateTime.monthValue,
                    localDateTime.year
                ).getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    /**
     * Change Altitude Temperature Pressure and UV
     */
    fun changeAltitudeTemperaturePressureUV(
        altitude: Double, temperature: Double, pressure: Double, uv: Double
    ): Boolean {
        val uvField = if (uv.roundToInt() in 0..5) uv.roundToInt() else 5
        val barometerField =
            if (pressure.roundToInt() in 0..299999) pressure.roundToInt() else 299999
        val altitudeField =
            if ((altitude * 10).roundToInt() in 0..9999) (altitude * 10).roundToInt() else 9999
        val temperatureField =
            if (temperature.roundToInt() in 0..99) temperature.roundToInt() else if (temperature.roundToInt() < 0) 0 else 99

        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value = ChangeUVTemperatureAltitudeBarometerPackage(
                uvField,
                temperatureField,
                altitudeField,
                barometerField
            ).getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    /**
     * Send call notification
     */
    fun sendCallNotification(): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                CallNotificationPackage().getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    /**
     * Send message notification
     */
    fun sendMessageNotification(): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value =
                MessageNotificationPackage().getPackage().toByteArray()
            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }

    /**
     * Send message notification
     */
    fun changeAlarm(localTime: LocalTime?): Boolean {
        return if (smartWatchWriteCharacteristic != null) {
            smartWatchWriteCharacteristic!!.value = if (localTime != null)
                ManageAlarmPackage(
                    true,
                    localTime.hour,
                    localTime.minute,
                    localTime.second
                ).getPackage().toByteArray()
            else
                ManageAlarmPackage(false).getPackage().toByteArray()

            bluetoothGatt!!.writeCharacteristic(smartWatchWriteCharacteristic)
        } else {
            false
        }
    }


    /**
     * Obtain pedometer info
     */
    fun addPedometerListener(pedometerCallback: (Int) -> Unit): Boolean {
        // TODO:
//        if(smartWatchNotificationCharacteristic !=null){
//            bluetoothGatt!!.setCharacteristicNotification(smartWatchNotificationCharacteristic, false)
//        }

//        val uuid: UUID = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
//        val descriptor = characteristic.getDescriptor(uuid).apply {
//            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//        }
//        bluetoothGatt.writeDescriptor(descriptor)

        return true
    }
}