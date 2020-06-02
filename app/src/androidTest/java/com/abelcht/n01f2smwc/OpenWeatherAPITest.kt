package com.abelcht.n01f2smwc

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abelcht.n01f2smwc.openwheatherapi.getTemperaturePressure
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class OpenWeatherAPITest {
    @Test
    fun testObtainTemperature() {
        val messagesToReceive = CountDownLatch(1);

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        getTemperaturePressure(41.545589, -0.851998, appContext) {
            if (it == null) {
                Log.i("OpenWeatherAPITest", "null")
            } else
                Log.i("OpenWeatherAPITest", "${it.first} ${it.second}")

            messagesToReceive.countDown();
        }

        val hasReceivedMessage = messagesToReceive.await(5, TimeUnit.SECONDS)

        if (!hasReceivedMessage)
            Log.i("OpenWeatherAPITest", "Message not received")
    }
}