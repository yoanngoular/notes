package com.ygoular.notes.di.module

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import com.ygoular.notes.di.scope.ApplicationScope
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val mApplication: Application) {

    @ApplicationScope
    @Provides
    fun provideApplication(): Application = mApplication

    @ApplicationScope
    @Provides
    fun provideAlarmManager(): AlarmManager =
        mApplication.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}