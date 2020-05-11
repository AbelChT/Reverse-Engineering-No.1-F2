package com.abelcht.n01f2smwc

import org.junit.Test

class MacConverterTest {
    @ExperimentalUnsignedTypes
    @Test
    fun testsWork() {
        val macAddress = "12:a2:33:12:a2:33"
        val addressSpliced = (0..5).map {
            macAddress.subSequence(3 * it, 3 * it + 2).toString().toInt(16).toByte()
        }
        val addressSpliced2 = macAddress.split(":").map { it.toInt(16).toByte() }.toTypedArray()
        println("Prueba:  ${addressSpliced[0]} ${addressSpliced[1]} ${addressSpliced[2]} ${addressSpliced[3]} ${addressSpliced[4]} ${addressSpliced[5]}")
        println("Prueba 2: ${0xff.toByte()} ${0xffu.toByte()}")
    }
}