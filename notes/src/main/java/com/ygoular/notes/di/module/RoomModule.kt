package com.ygoular.notes.di.module

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ygoular.notes.R
import com.ygoular.notes.database.AppDatabase
import com.ygoular.notes.database.dao.NoteDao
import com.ygoular.notes.database.entity.NoteEntity
import com.ygoular.notes.di.scope.ApplicationScope
import com.ygoular.notes.repository.NoteRepository
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class RoomModule(private val mApplication: Application) {

    private lateinit var mAppDatabase: AppDatabase

    private class PopulateDatabaseNoteAsyncTask(val mDatabase: AppDatabase, val context: Context) :
        AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg p0: Unit) {
            val noteDao = mDatabase.noteDao()
            noteDao.insert(
                NoteEntity(
                    context.getString(R.string.dummy_first_title),
                    context.getString(R.string.dummy_first_desc)
                )
            )
            noteDao.insert(
                NoteEntity(
                    context.getString(R.string.dummy_second_title),
                    context.getString(R.string.dummy_second_desc)
                )
            )
            noteDao.insert(
                NoteEntity(
                    context.getString(R.string.dummy_third_title),
                    context.getString(R.string.dummy_third_desc)
                )
            )
        }
    }

    @ApplicationScope
    @Provides
    fun provideAppDatabase(): AppDatabase {
        mAppDatabase = Room.databaseBuilder(
            mApplication,
            AppDatabase::class.java,
            "${mApplication.getString(R.string.app_name).toLowerCase(Locale.getDefault())}.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    PopulateDatabaseNoteAsyncTask(mAppDatabase, mApplication).execute()
                }
            })
            .build()

        return mAppDatabase
    }

    @ApplicationScope
    @Provides
    fun provideNoteDao(appDatabase: AppDatabase) = appDatabase.noteDao()

    @ApplicationScope
    @Provides
    fun provideNoteRepository(noteDao: NoteDao) = NoteRepository(noteDao)
}