package com.example.trackinglocationhelper.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val toast = Toast.makeText(
                context.applicationContext,
                TAG,
                Toast.LENGTH_LONG
            )
            toast.show()
            context.startService(Intent(context.applicationContext, LocationService::class.java))
        }
    }

    companion object {
        private const val TAG = "RestartServiceReceiver"
    }
}