package com.example.sporttracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.sporttracker.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getDatabase(context).supplementDao()
                val supplements = dao.getAllSupplementsOnce()
                for (supplement in supplements) {
                    planSupplementNotifications(context, supplement)
                }
            }
        }
    }
}
