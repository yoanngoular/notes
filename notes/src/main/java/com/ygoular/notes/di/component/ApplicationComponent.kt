package com.ygoular.notes.di.component

import android.app.AlarmManager
import android.app.Application
import com.ygoular.notes.database.AppDatabase
import com.ygoular.notes.database.dao.NoteDao
import com.ygoular.notes.di.module.ApplicationModule
import com.ygoular.notes.di.module.RoomModule
import com.ygoular.notes.di.scope.ApplicationScope
import com.ygoular.notes.repository.NoteRepository
import com.ygoular.notes.view.ui.NoteActivity
import dagger.Component

@ApplicationScope
@Component(modules = [ApplicationModule::class, RoomModule::class])
interface ApplicationComponent {
    fun database(): AppDatabase
    fun application(): Application
    fun noteRepository(): NoteRepository
    fun noteDao(): NoteDao
    fun alarmManager(): AlarmManager
}