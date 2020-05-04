package com.abelcht.n01f2smwc

import com.abelcht.n01f2smwc.smartwatch.communication.packages.ConfigurePackage
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalUnsignedTypes
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val cosas = ConfigurePackage().getPackage()

        cosas.forEach {
            println(it)
        }
    }
}
