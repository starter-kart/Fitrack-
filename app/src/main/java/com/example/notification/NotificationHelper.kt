package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    private const val CHANNEL_SEDENTARY_ID = "sedentary_warnings"
    private const val CHANNEL_SEDENTARY_NAME = "Sedentary Alerts"
    private const val CHANNEL_SEDENTARY_DESC = "Informs you when you have been sitting/inactive in one place too long"

    private const val CHANNEL_WATER_ID = "water_reminders"
    private const val CHANNEL_WATER_NAME = "Hydration Reminders"
    private const val CHANNEL_WATER_DESC = "Keeps you hydrated by reminding you to drink water"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Sedentary alert channel
            val sedentaryChannel = NotificationChannel(
                CHANNEL_SEDENTARY_ID,
                CHANNEL_SEDENTARY_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_SEDENTARY_DESC
                enableVibration(true)
            }

            // Hydration reminder channel
            val waterChannel = NotificationChannel(
                CHANNEL_WATER_ID,
                CHANNEL_WATER_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_WATER_DESC
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(sedentaryChannel)
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    fun showSedentaryNotification(context: Context, durationMinutes: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2301,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SEDENTARY_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Time to get up and stretch! 🚶‍♂️")
            .setContentText("You've been sedentary for over $durationMinutes minutes. A brief 1-minute walk will restart your metabolism and fire up focus!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(7001, builder.build())
    }

    fun showWaterReminderNotification(context: Context, goalMl: Int, currentMl: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2302,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = goalMl - currentMl
        val bodyContent = if (remaining > 0) {
            "Time to take a sip of water! Hydrate yourself. Goal: $goalMl ml, Current Intake: $currentMl ml. $remaining ml remaining!"
        } else {
            "Splendid work! You've met your daily hydration target of $goalMl ml! Keep up the amazing standards of performance!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_WATER_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hydration Alert 💧")
            .setContentText(bodyContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(7002, builder.build())
    }
}
