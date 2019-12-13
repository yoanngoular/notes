package com.ygoular.notes

import android.app.Application
import com.ygoular.notes.di.component.ApplicationComponent
import com.ygoular.notes.di.component.DaggerApplicationComponent
import com.ygoular.notes.di.module.ApplicationModule
import com.ygoular.notes.di.module.RoomModule

class BaseApplication : Application() {

    companion object {
        lateinit var mApplicationComponent: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        mApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .roomModule(RoomModule(this))
            .build()
    }
}