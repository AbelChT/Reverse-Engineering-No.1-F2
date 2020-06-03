package com.abelcht.n01f2smwc.broadcast.receiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ChangeDateTimeBroadcastReceiver(private val context: Context) : CustomBroadcastReceiver() {
    override fun configureAndRegisterReceiver() {
        val changeDateTimeIntentFilter = IntentFilter()
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIME_TICK)
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        changeDateTimeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED)
        context.registerReceiver(
            this,
            changeDateTimeIntentFilter
        )
    }

    override fun unRegisterReceiver() {
        context.unregisterReceiver(this)
    }
}