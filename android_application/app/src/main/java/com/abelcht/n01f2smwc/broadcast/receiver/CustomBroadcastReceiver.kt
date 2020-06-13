package com.abelcht.n01f2smwc.broadcast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class CustomBroadcastReceiver : BroadcastReceiver() {
    private var localCallback: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (localCallback != null) {
            localCallback!!.invoke()
        }
    }

    fun setCallback(callback: (() -> Unit)) {
        localCallback = callback
    }

    abstract fun configureAndRegisterReceiver()

    abstract fun unRegisterReceiver()
}