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
        val distance = reassembledPackage[reassembledPackage.size - 5].toUInt() +
                reassembledPackage[reassembledPackage.size - 4].toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 3].toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 2].toUByte()
                    .toUInt() *
                (2.0.pow(16).toUInt())

        val kcal = reassembledPackage[reassembledPackage.size - 9].toUInt() +
                reassembledPackage[reassembledPackage.size - 8].toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 7].toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 6].toUInt() *
                (2.0.pow(24).toUInt())

        val steps = reassembledPackage[reassembledPackage.size - 13].toUInt() +
                reassembledPackage[reassembledPackage.size - 12].toUInt() *
                (2.0.pow(8).toUInt()) +
                reassembledPackage[reassembledPackage.size - 11].toUInt() *
                (2.0.pow(16).toUInt()) +
                reassembledPackage[reassembledPackage.size - 10].toUInt() *
                (2.0.pow(24).toUInt())
        return steps.toInt()
    }

    @ExperimentalUnsignedTypes
    @Test
    fun testPedometerMessageAnalyzer() {
        val pedometerMessage = arrayOf(
            169, 33, 0, 34, 20, 6, 4, 2, 1, 0, 59, 215, 16, 0, 0, 177, 5, 0, 0, 41, 1, 0, 0, 1, 0,
            59, 12, 17, 0, 0, 195, 5, 0, 0, 45, 1, 0, 0, 94
        )

        analyzePedometerPackage(pedometerMessage.map { it.toByte() })

    }
}