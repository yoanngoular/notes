package com.ygoular.notes.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ygoular.notes.R
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.helper.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    private val notificationHelper = NotificationHelper()

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(NoteEntity.EXTRA_NOTE_TITLE)
        val description = intent.getStringExtra(NoteEntity.EXTRA_NOTE_DESCRIPTION)
        val id = intent.getIntExtra(NoteEntity.EXTRA_REMINDER_ID, 0)

        if (title.isNullOrEmpty() || id == 0) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationHelper.createNotificationChannel(
            notificationManager,
            context.getString(R.string.notification_channel_name)
        )

        notificationManager.notify(
            id,
            notificationHelper.buildNotification(title, description, context)
        )
    }
}