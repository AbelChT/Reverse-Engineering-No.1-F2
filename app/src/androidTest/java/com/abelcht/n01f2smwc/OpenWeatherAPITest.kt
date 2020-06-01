package com.abelcht.n01f2smwc

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abelcht.n01f2smwc.openwheatherapi.getTemperaturePressureUV
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OpenWeatherAPITest {
    @Test
    fun testObtainTemperature() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        getTemperaturePressureUV(41.545589, -0.851998, appContext)

    }
}