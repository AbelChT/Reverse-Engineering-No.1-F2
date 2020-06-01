package com.abelcht.n01f2smwc.smartwatch.communication.packages;

class ChangeDateTimePackage(
    hours: Int, minutes: Int, seconds: Int, day: Int, month: Int, year: Int
) : SmartWatchPackage(
    0x01.toByte(),
    arrayOf(
        (year - 2000).toByte(), month.toByte(), day.toByte(), hours.toByte(), minutes.toByte(),
        seconds.toByte()
    )
)