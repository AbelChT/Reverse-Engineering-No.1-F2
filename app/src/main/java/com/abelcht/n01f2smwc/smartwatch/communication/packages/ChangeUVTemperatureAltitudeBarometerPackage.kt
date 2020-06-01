package com.abelcht.n01f2smwc.smartwatch.communication.packages;

class ChangeUVTemperatureAltitudeBarometerPackage(
    uv: Int, temperature: Int, altitude: Int, barometer: Int
) : SmartWatchPackage(
    0x1b.toByte(),
    arrayOf(
        (barometer % 256).toByte(), ((barometer / 256) % 256).toByte(),
        ((barometer / (256 * 256)) % 256).toByte(), 0x00.toByte(), // Barometer
        (altitude % 256).toByte(), ((altitude / 256) % 256).toByte(), // Altitude
        ((temperature * 10) % 256).toByte(),
        (((temperature * 10) / 256) % 256).toByte(), // Temperature
        (if (uv in 1..5) uv else 6).toByte() // UV
    )
)