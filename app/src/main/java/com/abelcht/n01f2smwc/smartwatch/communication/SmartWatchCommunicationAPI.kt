package com.abelcht.n01f2smwc.smartwatch.communication

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.abelcht.n01f2smwc.smartwatch.communication.packages.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.math.pow
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

    // Pedometer callback
    private var pedometerCallback: ((Int) -> Unit)? = null

    /**
     * Analyze the pedometer information and return the steps walked
     */
    @ExperimentalUnsignedTypes
    private fun analyzePedometerPackage(reassembledPackage: List<Byte>): Int {
        // Pedometer package
        /*
        val distance = reassembledPackage[reassembledPackage.size - 5].toUByte().toUInt() +
                reassembledPackage[reassembledPackage.size - 4].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 3].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 2].toUByte().toUInt() *
                (2.0.pow(16).toUInt())

        val kcal = reassembledPackage[reassembledPackage.size - 9].toUByte().toUInt() +
                reassembledPackage[reassembledPackage.size - 8].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 7].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 6].toUByte().toUInt() *
                (2.0.pow(24).toUInt())
         */

        val steps = reassembledPackage[reassembledPackage.size - 13].toUByte().toUInt() +
                reassembledPackage[reassembledPackage.size - 12].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 11].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 10].toUByte().toUInt() *
                (2.0.pow(24).toUInt())
        return steps.toInt()
    }

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

        // True if we are reassembling a package
        var isReassemblingPackage: Boolean = false

        // Package that are been reassembled
        val reassembledPackage = arrayListOf<Byte>()

        // Bytes to finish the package
        var bytesToRead = 0

        fun bytesToString(bytes: List<Byte>): String {
            var finalString = "{"
            for (j in bytes) {
                finalString += "${j.toUByte().toInt()}, "
            }
            return "$finalString}"
        }

        private fun processNotificationMessage(messageValue: Array<Byte>) {
            // Reassemble package
            if (isReassemblingPackage) {
                reassembledPackage.addAll(messageValue)
                if (bytesToRead != messageValue.size) {
                    bytesToRead -= messageValue.size
                } else {
                    isReassemblingPackage = false
                    bytesToRead = 0
                }
            } else if (messageValue[0] == 0xa9.toByte()) {
                // New package with correct header
                val packageExpectedSize =
                    4 + messageValue[3].toInt() + 1  // header size + content size + crc
                reassembledPackage.clear()
                reassembledPackage.addAll(messageValue)
                if (packageExpectedSize != messageValue.size) {
                    bytesToRead = packageExpectedSize - messageValue.size
                    isReassemblingPackage = true
                }
            } else {
                //New package with wrong header
                bytesToRead = 0
                reassembledPackage.clear()
                Log.i(logTag, "Error in received message: Wrong header")
            }

            if (bytesToRead < 0) {
                // Error in message
                bytesToRead = 0
                isReassemblingPackage = false
                reassembledPackage.clear()
                Log.i(logTag, "Error in received message: Exceeded length")
            } else if (!isReassemblingPackage) {
                // Analyze package
                when {
                    reassembledPackage[1] == 0x21.toByte() -> {
                        // Pedometer package
                        val steps = analyzePedometerPackage(reassembledPackage)
                        Log.i(
                            logTag,
                            "Pedometer package received and assembled ${bytesToString(
                                reassembledPackage
                            )} steps: $steps"
                        )

                        if (pedometerCallback != null) {
                            pedometerCallback!!.invoke(steps)
                        }

                    }
                    reassembledPackage[1] == 0x0f.toByte() -> {
                        Log.i(
                            logTag,
                            "Received photo action package, but won't be used ${bytesToString(
                                reassembledPackage
                            )}"
                        )
                    }
                    else -> {
                        Log.i(
                            logTag,
                            "Unrecognised package received (well formatted) ${bytesToString(
                                reassembledPackage
                            )}"
                        )
                    }
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
                    val configureResult = configureSmartWatch()
                    Log.i(logTag, "Configure Smartwatch result $configureResult")

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
            if (smartWatchNotificationCharacteristic!!.uuid.toString() == characteristic!!.uuid.toString() && characteristic.value != null
            )
                processNotificationMessage(characteristic.value.toTypedArray())
        }
    }

    /**
     * Connect to the SmartWatch
     */
    @ExperimentalUnsignedTypes
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
    fun changePedometerListener(pedometerCallback: ((Int) -> Unit)?) {
        this.pedometerCallback = pedometerCallback
    }

}