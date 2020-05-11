package com.abelcht.n01f2smwc.smartwatch.communication.packages

class ConfigurePackage(bluetoothAddress: String) : SmartWatchPackage(
    0x32.toByte(),
    bluetoothAddress.split(":").map { it.toInt(16).toByte() }.toTypedArray()
)