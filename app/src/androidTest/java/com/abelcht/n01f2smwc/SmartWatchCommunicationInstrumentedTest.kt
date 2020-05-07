package com.abelcht.n01f2smwc

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
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

        val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager =
                appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        assert(bluetoothAdapter != null)

        val scanCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                println("onScanFailed")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                println("onScanResult")
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                println("onBatchScanResults")
            }
        }

        val bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
        bluetoothLeScanner.startScan(scanCallback)
        println("Sleep start")
        Thread.sleep(10000)
        println("Sleep end")
        bluetoothLeScanner.stopScan(scanCallback)
    }
}
