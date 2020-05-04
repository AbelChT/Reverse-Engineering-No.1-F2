package com.abelcht.n01f2smwc.smartwatch.communication.packages

import com.abelcht.n01f2smwc.smartwatch.config.DeviceInfo

@ExperimentalUnsignedTypes
class ConfigurePackage : SmartWatchPackage(0x32u, DeviceInfo.SmartWatchConfigurationPackage)