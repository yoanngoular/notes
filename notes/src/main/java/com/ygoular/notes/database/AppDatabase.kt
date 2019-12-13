package com.ygoular.notes.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ygoular.notes.database.AppDatabase.Companion.VERSION
import com.ygoular.notes.database.converter.Converters
import com.ygoular.notes.database.dao.NoteDao
import com.ygoular.notes.database.entity.NoteEntity

/**
 * Database using Room
 */
@Database(entities = [NoteEntity::class], version = VERSION)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 1
    }

    abstract fun noteDao(): NoteDao
}
