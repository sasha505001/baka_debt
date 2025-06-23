package com.example.sporttracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.sporttracker.data.model.Supplement
import java.util.Calendar


fun scheduleNotification(context: Context, title: String, message: String, timeInMillis: Long) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        timeInMillis.toInt(), // уникальный requestCode
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    Toast.makeText(
                        context,
                        "Точные уведомления отключены. Проверьте настройки энергосбережения.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        }

    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Ошибка доступа к уведомлениям: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}

fun planSupplementNotifications(context: Context, supplement: Supplement) {
    // Заглушка для восстановления уведомлений
}
