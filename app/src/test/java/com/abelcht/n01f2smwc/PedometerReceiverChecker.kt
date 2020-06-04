package com.abelcht.n01f2smwc

import org.junit.Test
import kotlin.math.pow

class PedometerReceiverChecker {

    @ExperimentalUnsignedTypes
    fun analyzePedometerPackage(reassembledPackage: List<Byte>) {
        // Pedometer package
        val distance =
            reassembledPackage[reassembledPackage.size - 4].toUByte().toInt() +
                    reassembledPackage[reassembledPackage.size - 3].toUByte()
                        .toInt() *
                    (2.0.pow(8).toInt()) +
                    reassembledPackage[reassembledPackage.size - 2].toUByte()
                        .toInt() *
                    (2.0.pow(16).toInt())

        val kcal =
            reassembledPackage[reassembledPackage.size - 8].toUByte().toInt() +
                    reassembledPackage[reassembledPackage.size - 7].toUByte()
                        .toInt() *
                    (2.0.pow(8).toInt()) +
                    reassembledPackage[reassembledPackage.size - 6].toUByte()
                        .toInt() *
                    (2.0.pow(16).toInt()) +
                    reassembledPackage[reassembledPackage.size - 5].toUByte()
                        .toInt() *
                    (2.0.pow(24).toInt())

        val steps =
            reassembledPackage[reassembledPackage.size - 12].toUByte().toInt() +
                    reassembledPackage[reassembledPackage.size - 11].toUByte()
                        .toInt() *
                    (2.0.pow(8).toInt()) +
                    reassembledPackage[reassembledPackage.size - 10].toUByte()
                        .toInt() *
                    (2.0.pow(16).toInt()) +
                    reassembledPackage[reassembledPackage.size - 9].toUByte()
                        .toInt() *
                    (2.0.pow(24).toInt())
        println(
            "distace : $distance kcal: $kcal steps: $steps"
        )
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