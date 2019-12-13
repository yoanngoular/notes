package com.ygoular.notes.helper

import android.content.Context
import android.content.SharedPreferences
import com.ygoular.notes.BaseApplication
import com.ygoular.notes.BuildConfig
import com.ygoular.notes.model.SortingStrategy

object PrefsHelper {

    private const val SHARED_PREFERENCES_NAME: String = "${BuildConfig.APPLICATION_ID}-preferences"

    val mSharedPreferences: SharedPreferences by lazy {
        BaseApplication.mApplicationComponent.application()
            .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    const val PREF_SORTING_STRATEGY: String = "SORTING_STRATEGY"
    var PREF_SORTING_STRATEGY_DEFAULT: SortingStrategy = SortingStrategy.PRIORITY

    private inline fun edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = mSharedPreferences.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    inline operator fun <reified T : Any> get(key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> mSharedPreferences.getString(key, defaultValue as String) as T
            Int::class -> mSharedPreferences.getInt(key, defaultValue as Int) as T
            Boolean::class -> mSharedPreferences.getBoolean(key, defaultValue as Boolean) as T
            Float::class -> mSharedPreferences.getFloat(key, defaultValue as Float) as T
            Long::class -> mSharedPreferences.getLong(key, defaultValue as Long) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}