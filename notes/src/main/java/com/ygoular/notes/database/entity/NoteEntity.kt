package com.ygoular.notes.database.entity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ygoular.notes.receiver.AlarmReceiver
import com.ygoular.notes.BaseApplication
import com.ygoular.notes.model.Reminder
import java.util.Calendar
import java.util.Date

/**
 * Main table of the database
 *
 * Represents a note with all its attributes. Its id is auto generated
 * and auto incremented by the database.
 *
 * All tables need to be declared in the database class (AppDatabase)
 * using the dedicated annotation "@Database(entities = [NoteEntity::class]".
 *
 */
@Entity(tableName = "note_table")
data class NoteEntity constructor(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var mId: Long?,
    @ColumnInfo(name = "title") var mTitle: String,
    @ColumnInfo(name = "description") var mDescription: String,
    @ColumnInfo(name = "priority") var mPriority: Int,
    @ColumnInfo(name = "date") val mDate: Date,
    @ColumnInfo(name = "reminders") var mReminders: MutableSet<Reminder>
) {
    constructor() : this(
        null,
        "",
        "",
        Priority.MIN.value,
        Calendar.getInstance().time,
        mutableSetOf<Reminder>()
    )

    constructor(title: String, description: String) :
            this(
                null,
                title,
                description,
                Priority.MIN.value,
                Calendar.getInstance().time,
                mutableSetOf<Reminder>()
            )

    constructor(title: String, description: String, priority: Priority) :
            this(
                null,
                title,
                description,
                priority.value,
                Calendar.getInstance().time,
                mutableSetOf<Reminder>()
            )

    constructor(
        title: String,
        description: String,
        priority: Priority,
        reminders: MutableSet<Reminder>
    ) :
            this(null, title, description, priority.value, Calendar.getInstance().time, reminders)

    companion object {
        const val WRONG_ID: Long = -1L
        const val MAX_NUMBER_OF_REMINDERS = 5

        const val EXTRA_NOTE_TITLE = "extra_note_title"
        const val EXTRA_NOTE_DESCRIPTION = "extra_note_description"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"

        private val mApplicationContext: Context by lazy {
            BaseApplication.mApplicationComponent.application()
        }
        private val mAlarmManager: AlarmManager by lazy {
            BaseApplication.mApplicationComponent.alarmManager()
        }
    }

    fun isEquivalent(noteToCompare: NoteEntity): Boolean {
        return mTitle == noteToCompare.mTitle
                && mDescription == noteToCompare.mDescription
                && mPriority == noteToCompare.mPriority
                && mReminders == noteToCompare.mReminders
    }

    fun setUpReminders() {
        mReminders.forEach {
            if (it.mDate.after(Calendar.getInstance().time)) {
                mAlarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it.mDate.time,
                    createPendingIntent(it.mId)
                )
            }
        }
    }

    fun cancelReminders() {
        mReminders.forEach {
            if (it.mDate.after(Calendar.getInstance().time)) {
                mAlarmManager.cancel(createPendingIntent(it.mId))
            }
        }
    }

    fun getNextReminder(): Reminder? {
        return mReminders.filter { it.mDate.after(Calendar.getInstance().time) }
            .minBy { it.mDate.time }
    }

    private fun createPendingIntent(id: Int): PendingIntent {
        val intent = with(Intent(mApplicationContext, AlarmReceiver::class.java)) {
            putExtra(EXTRA_REMINDER_ID, id)
            putExtra(EXTRA_NOTE_TITLE, mTitle)
            putExtra(EXTRA_NOTE_DESCRIPTION, mDescription)
        }

        return PendingIntent.getBroadcast(
            mApplicationContext,
            id,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}

@Suppress("unused")
enum class Priority(val value: Int) {
    MIN(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    MAX(4);

    companion object {
        private val map = values().associateBy(Priority::value)
        fun fromValue(value: Int) = map[value] ?: MIN
    }
}