package com.example.group12_asgmt2

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ExpiryNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!ExpiryNotificationManager.isEnabled(applicationContext)) return Result.success()
        if (!canPostNotifications()) return Result.success()

        return try {
            val expiringItems = IngredientRepository.getAll().filter { ingredient ->
                ingredient.alertEnabled != false &&
                    DateUtils.getExpiryStatus(ingredient.expiryDate) != DateUtils.ExpiryStatus.FRESH
            }

            if (expiringItems.isEmpty()) {
                try {
                    NotificationManagerCompat.from(applicationContext)
                    .cancel(NOTIFICATION_ID)
                } catch (_: SecurityException) {
                    // Permission may have been revoked after the check above.
                }
                return Result.success()
            }

            val lines = expiringItems.take(MAX_DISPLAYED_ITEMS).map { ingredient ->
                val days = DateUtils.daysUntilExpiry(ingredient.expiryDate)
                when {
                    days < 0 -> applicationContext.getString(
                        R.string.expiry_notification_expired_item,
                        ingredient.name
                    )
                    days == 0L -> applicationContext.getString(
                        R.string.expiry_notification_today_item,
                        ingredient.name
                    )
                    else -> applicationContext.getString(
                        R.string.expiry_notification_days_item,
                        ingredient.name,
                        days
                    )
                }
            }

            val openAppIntent = Intent(applicationContext, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = applicationContext.resources.getQuantityString(
                R.plurals.expiry_notification_title,
                expiringItems.size,
                expiringItems.size
            )
            val notification = NotificationCompat.Builder(
                applicationContext,
                ExpiryNotificationManager.CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(lines.first())
                .setStyle(NotificationCompat.InboxStyle().also { style ->
                    lines.forEach(style::addLine)
                    if (expiringItems.size > lines.size) {
                        style.setSummaryText(
                            applicationContext.getString(
                                R.string.expiry_notification_more_items,
                                expiringItems.size - lines.size
                            )
                        )
                    }
                })
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(applicationContext, R.color.green_primary))
                .build()

            try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Permission may have been revoked while this worker was running.
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val MAX_DISPLAYED_ITEMS = 5
    }
}
