package com.abelcht.n01f2smwc.broadcast.receiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager

class NewCallBroadcastReceiver(private val context: Context) : CustomBroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
            super.onReceive(context, intent)
        }
    }

    override fun configureAndRegisterReceiver() {
        val newCallIntentFilter = IntentFilter()
        newCallIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(this, newCallIntentFilter)
    }

    override fun unRegisterReceiver() {
        context.unregisterReceiver(this)
    }
}