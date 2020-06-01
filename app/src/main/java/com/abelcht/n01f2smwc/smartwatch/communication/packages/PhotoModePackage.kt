package com.abelcht.n01f2smwc.smartwatch.communication.packages;

class PhotoModePackage(enable: Boolean) :
    SmartWatchPackage(0x1c.toByte(), arrayOf(if (enable) 0x01.toByte() else 0x00.toByte()))