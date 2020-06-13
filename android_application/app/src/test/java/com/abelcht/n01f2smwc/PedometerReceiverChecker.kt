package com.abelcht.n01f2smwc

import org.junit.Test
import kotlin.math.pow

class PedometerReceiverChecker {


    /**
     * Analyze the pedometer information and return the steps walked
     */
    @ExperimentalUnsignedTypes
    fun analyzePedometerPackage(reassembledPackage: List<Byte>): Int {
        // Pedometer package
        val distance = reassembledPackage[reassembledPackage.size - 5].toUByte().toUInt() +
                reassembledPackage[reassembledPackage.size - 4].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 3].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 2].toUByte().toUInt() *
                (2.0.pow(16).toUInt())

        val kcal = reassembledPackage[reassembledPackage.size - 9].toUInt() +
                reassembledPackage[reassembledPackage.size - 8].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 7].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 6].toUByte().toUInt() *
                (2.0.pow(24).toUInt())

        val steps = reassembledPackage[reassembledPackage.size - 13].toUByte().toUInt() +
                reassembledPackage[reassembledPackage.size - 12].toUByte().toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 11].toUByte().toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 10].toUByte().toUInt() *
                (2.0.pow(24).toUInt())
        return steps.toInt()
    }

    @ExperimentalUnsignedTypes
    @Test
    fun testPedometerMessageAnalyzer() {
        val pedometerMessage = arrayOf(
            169, 33, 0, 49, 20, 6, 8, 3, 1, 0, 37, 10, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 42,
            168, 0, 0, 0, 56, 0, 0, 0, 11, 0, 0, 0, 1, 0, 43, 223, 0, 0, 0, 75, 0, 0, 0, 15, 0, 0,
            0, 206
        )

        val steps = analyzePedometerPackage(pedometerMessage.map { it.toByte() })

        println(steps)

    }
}