package com.ygoular.notes.database.converter

import androidx.room.TypeConverter
import com.ygoular.notes.model.Reminder
import com.google.gson.Gson
import java.util.Date

/**
 * Converters for Room database
 *
 * Automatically converts objects to a serialized form when
 * added to the database. It also deserializes those objects
 * when requested in the code.
 *
 * For this functionality to work, it is necessary to indicate
 * to the database class (AppDatabase) to use this class with
 * the dedicated annotation "@TypeConverters(Converters::class)".
 *
 */
class Converters {

    companion object {
        const val REMINDER_LIST_DELIMITER = "#"
    }

    @TypeConverter
    fun fromTimestamp(timestamp: Long): Date = Date(timestamp)

    @TypeConverter
    fun dateToTimestamp(date: Date): Long = date.time

    @TypeConverter
    fun fromString(serializedReminders: String): MutableSet<Reminder> {
        if (serializedReminders.isEmpty()) {
            return mutableSetOf()
        }

        return if (!serializedReminders.contains(REMINDER_LIST_DELIMITER)) {
            listOf(Gson().fromJson(serializedReminders, Reminder::class.java)).toMutableSet()
        } else { // More than 1 reminder in its serialized form
            serializedReminders.split(REMINDER_LIST_DELIMITER).map {
                Gson().fromJson(it, Reminder::class.java)
            }.toMutableSet()
        }
    }

    @TypeConverter
    fun remindersToString(reminderList: MutableSet<Reminder>): String =
        reminderList.joinToString(REMINDER_LIST_DELIMITER) { Gson().toJson(it) }
}