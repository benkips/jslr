package com.mabnets.jslradio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LaunchPlayerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifyIntent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(notifyIntent)
    }
}