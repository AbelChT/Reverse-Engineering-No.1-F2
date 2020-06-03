package com.abelcht.n01f2smwc.broadcast.receiver

import android.content.Context
import android.content.IntentFilter
import android.provider.Telephony

class NewMessageBroadcastReceiver(private val context: Context) : CustomBroadcastReceiver() {
    override fun configureAndRegisterReceiver() {
        val newMessageIntentFilter = IntentFilter()
        newMessageIntentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        // TODO Improvement: May add messaging app intents too
        context.registerReceiver(this, newMessageIntentFilter)
    }

    override fun unRegisterReceiver() {
        context.unregisterReceiver(this)
    }
}