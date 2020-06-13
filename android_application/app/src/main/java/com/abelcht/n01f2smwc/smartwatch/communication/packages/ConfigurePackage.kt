package com.abelcht.n01f2smwc.smartwatch.communication.packages

class ConfigurePackage(bluetoothAddress: String) : SmartWatchPackage(
    0x32.toByte(), arrayOf(
        0x40.toByte(), 0xe2.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
    ) + bluetoothAddress.split(":").map { it.toInt(16).toByte() }.toTypedArray()
)