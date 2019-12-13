package com.ygoular.notes.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ygoular.notes.BuildConfig
import com.ygoular.notes.R
import com.ygoular.notes.view.ui.NoteActivity

class NotificationHelper {

    companion object {
        private const val CHANNEL_ID: String = BuildConfig.APPLICATION_ID
    }

    fun createNotificationChannel(
        notificationManager: NotificationManager,
        channelName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    fun buildNotification(
        title: String?,
        message: String?,
        context: Context
    ): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(R.drawable.ic_reminder_on)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setAutoCancel(true)
            setCategory(NotificationCompat.CATEGORY_REMINDER)
            setContentIntent(buildNotificationIntent(context))
            priority = NotificationCompat.PRIORITY_MAX
        }.build()
    }

    private fun buildNotificationIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, NoteActivity::class.java),
            0
        )
    }
}