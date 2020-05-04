package com.abelcht.n01f2smwc

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class SmartWatchCommunicationInstrumentedTest {
    @Test
    fun sendSmartWatchMessageNotification() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.abelcht.n01f2smwc", appContext.packageName)
    }
}
