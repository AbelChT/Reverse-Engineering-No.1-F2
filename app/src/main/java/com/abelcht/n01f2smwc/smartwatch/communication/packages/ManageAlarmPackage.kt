package com.abelcht.n01f2smwc.smartwatch.communication.packages;

class ManageAlarmPackage(enable: Boolean, hours: Int = 0, minutes: Int = 0, seconds: Int = 0) :
    SmartWatchPackage(
        0x02.toByte(),
        if (enable) arrayOf(hours.toByte(), minutes.toByte(), seconds.toByte()) else arrayOf()
    )