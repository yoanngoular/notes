package com.ygoular.notes.model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
// TODO: Add periodicity in reminders
data class Reminder(val mId: Int, val mDate: Date) {

    companion object {
        const val INVALID_ID: Int = 0
        const val TIME_IN_SECONDS: Long = 1000L

        fun formatDate(reminder: Reminder): String {
            return formatDate(reminder.mDate)
        }

        fun formatDate(date: Date): String {
            var formattedDate =
                SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date)

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            calendar.time = date

            if (currentYear == calendar.get(Calendar.YEAR)) {
                formattedDate = formattedDate.replace(" $currentYear", "")
            }
            return formattedDate
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val reminder: Reminder = other as Reminder
        return (this.mDate.time / TIME_IN_SECONDS) == (reminder.mDate.time / TIME_IN_SECONDS)
    }

    override fun hashCode(): Int {
        return (mDate.time / TIME_IN_SECONDS).toInt()
    }
}