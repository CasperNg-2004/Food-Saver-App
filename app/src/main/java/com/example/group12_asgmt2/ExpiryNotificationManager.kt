package com.example.group12_asgmt2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ExpiryNotificationManager {
    const val CHANNEL_ID = "expiry_alerts"
    private const val PERIODIC_WORK_NAME = "daily_expiry_check"
    private const val PREFERENCES_NAME = "expiry_notification_preferences"
    private const val KEY_ENABLED = "enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()

        if (enabled) {
            schedule(context)
            runNow(context)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            context.getSystemService(NotificationManager::class.java)
                .cancel(EXPIRY_NOTIFICATION_ID)
        }
    }

    fun schedule(context: Context) {
        if (!isEnabled(context)) return
        createChannel(context)

        val now = ZonedDateTime.now()
        var nextRun = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) nextRun = nextRun.plusDays(1)
        val initialDelayMinutes = Duration.between(now, nextRun).toMinutes()

        val request = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
            24,
            TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runNow(context: Context) {
        if (!isEnabled(context)) return
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<ExpiryNotificationWorker>().build())
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.expiry_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.expiry_notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private const val EXPIRY_NOTIFICATION_ID = 1001
}
